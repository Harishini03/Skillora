import { startTransition, useDeferredValue, useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import api from "../lib/api";
import { ProgressBar, StatCard } from "../components/AppShell";
import { downloadCsv } from "../lib/csv";
import { isFirebaseConfigured, saveFirebaseRecord } from "../lib/firebaseStore";

const APPLICATION_STATUSES = [
  "ALL",
  "APPLIED",
  "SHORTLISTED",
  "INTERVIEW_SCHEDULED",
  "OFFERED",
  "REJECTED",
  "WITHDRAWN",
];

const RECOMMENDATIONS = ["Strong Hire", "Hire", "Hold", "Reject"];
const INTERVIEW_MODES = ["ONLINE", "ONSITE"];

const statusClass = (status) => {
  const normalized = (status || "").toUpperCase();
  if (normalized === "OFFERED") return "bg-emerald-100 text-emerald-800";
  if (normalized === "INTERVIEW_SCHEDULED") return "bg-cyan-100 text-cyan-800";
  if (normalized === "SHORTLISTED") return "bg-amber-100 text-amber-800";
  if (normalized === "REJECTED" || normalized === "WITHDRAWN") return "bg-rose-100 text-rose-800";
  return "bg-slate-100 text-slate-700";
};

const formatDateTime = (value) => {
  if (!value) return "-";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "-";
  return `${date.toLocaleDateString()} ${date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}`;
};

const readPersistentList = (key) => {
  try {
    const raw = localStorage.getItem(key);
    const parsed = raw ? JSON.parse(raw) : [];
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
};

const readSessionSnapshot = () => {
  try {
    const raw = localStorage.getItem("pi_session");
    return raw ? JSON.parse(raw) : {};
  } catch {
    return {};
  }
};

const staffStorageKey = (suffix) => {
  const session = readSessionSnapshot();
  return `staff/${session.userId || "local"}/${suffix}`;
};

const flattenTopStudents = (top) => Object.entries(top || {})
  .flatMap(([department, students]) => (students || []).map((student) => ({ ...student, department })));

const exportStudentsCsv = (filename, students) => {
  downloadCsv(
    filename,
    ["studentId", "name", "department", "readiness", "score"],
    students.map((student) => [
      student.studentId,
      student.name,
      student.department,
      student.readiness ?? "",
      student.score ?? "",
    ]),
  );
};

const NotificationPanel = ({ notifications, markRead, loading }) => (
  <div className="dashboard-card p-5">
    <div className="flex items-center justify-between gap-2">
      <h3 className="text-lg font-semibold text-slate-900">Notifications</h3>
      {loading ? <span className="text-xs text-slate-500">Refreshing...</span> : null}
    </div>
    <ul className="mt-3 space-y-2 text-sm">
      {notifications.length === 0 ? <li className="text-slate-600">No notifications.</li> : null}
      {notifications.map((item) => (
        <li key={item.notificationId} className={`rounded-xl border px-3 py-2 ${item.read ? "border-slate-200 bg-slate-50 text-slate-600" : "border-cyan-200 bg-cyan-50 text-cyan-900"}`}>
          <div className="flex items-start justify-between gap-3">
            <div>
              <p className="text-xs font-semibold uppercase tracking-wide">{item.type}</p>
              <p className="mt-1">{item.message}</p>
              <p className="mt-1 text-xs text-slate-500">{formatDateTime(item.createdAt)}</p>
            </div>
            {!item.read ? (
              <button
                type="button"
                className="rounded-lg border border-cyan-300 px-2 py-1 text-xs font-semibold text-cyan-800"
                onClick={() => markRead(item.notificationId)}
              >
                Mark read
              </button>
            ) : null}
          </div>
        </li>
      ))}
    </ul>
  </div>
);

const usePortalNotifications = () => {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  const refresh = async () => {
    setLoading(true);
    try {
      const { data } = await api.get("/api/portal/common/notifications");
      setNotifications(Array.isArray(data) ? data : []);
    } catch {
      setNotifications([]);
    } finally {
      setLoading(false);
    }
  };

  const markRead = async (notificationId) => {
    try {
      await api.patch(`/api/portal/common/notifications/${notificationId}/read`);
      startTransition(() => {
        setNotifications((prev) => prev.map((item) => (item.notificationId === notificationId ? { ...item, read: true } : item)));
      });
    } catch {
      // no-op
    }
  };

  useEffect(() => {
    refresh();
  }, []);

  return { notifications, loading, refresh, markRead };
};

const RiskBadge = ({ readiness }) => {
  const score = Number(readiness || 0);
  const tier = score >= 75 ? "Low Risk" : score >= 55 ? "Moderate Risk" : "High Risk";
  const tone = score >= 75 ? "bg-emerald-100 text-emerald-800" : score >= 55 ? "bg-amber-100 text-amber-800" : "bg-rose-100 text-rose-800";
  return <span className={`rounded-full px-2 py-1 text-xs font-semibold ${tone}`}>{tier}</span>;
};

export const RecruiterDashboardPage = () => {
  const navigate = useNavigate();
  const [dashboard, setDashboard] = useState(null);
  const [jobInsights, setJobInsights] = useState({});
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [form, setForm] = useState({
    title: "",
    description: "",
    location: "",
    compensation: "",
    minCgpa: "6.5",
    requiredSkills: "",
    jobType: "FULL_TIME",
  });
  const { notifications, loading: notificationsLoading, markRead, refresh: refreshNotifications } = usePortalNotifications();

  const loadDashboard = async () => {
    setLoading(true);
    setError("");
    try {
      const { data } = await api.get("/api/portal/recruiter/dashboard");
      setDashboard(data);
      const jobs = Array.isArray(data?.jobs) ? data.jobs : [];
      if (jobs.length === 0) {
        setJobInsights({});
      } else {
        const entries = await Promise.all(
          jobs.map(async (job) => {
            try {
              const { data: apps } = await api.get("/api/portal/recruiter/applications", { params: { jobId: job.jobId } });
              const list = Array.isArray(apps) ? apps : [];
              const counts = list.reduce((acc, item) => {
                const key = item.status || "UNKNOWN";
                acc[key] = (acc[key] || 0) + 1;
                return acc;
              }, {});
              return [job.jobId, { total: list.length, counts }];
            } catch {
              return [job.jobId, { total: 0, counts: {} }];
            }
          }),
        );
        setJobInsights(Object.fromEntries(entries));
      }
    } catch (e) {
      setError(e.response?.data?.message || "Failed to load recruiter dashboard.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboard();
  }, []);

  const createJob = async (event) => {
    event.preventDefault();
    setCreating(true);
    setError("");
    setMessage("");
    try {
      await api.post("/api/portal/recruiter/jobs", {
        title: form.title.trim(),
        description: form.description.trim(),
        location: form.location.trim(),
        compensation: form.compensation.trim(),
        minCgpa: form.minCgpa ? Number(form.minCgpa) : null,
        requiredSkills: form.requiredSkills.trim(),
        jobType: form.jobType,
      });
      setForm({
        title: "",
        description: "",
        location: "",
        compensation: "",
        minCgpa: "6.5",
        requiredSkills: "",
        jobType: "FULL_TIME",
      });
      setMessage("Job posted successfully.");
      await Promise.all([loadDashboard(), refreshNotifications()]);
    } catch (e) {
      setError(e.response?.data?.message || "Failed to create job.");
    } finally {
      setCreating(false);
    }
  };

  if (loading) return (
    <div className="space-y-4">
      <div className="portal-banner animate-pulse"><div className="h-6 w-48 rounded bg-white/20 mb-2"/><div className="h-8 w-72 rounded bg-white/20"/></div>
      <div className="grid gap-4 md:grid-cols-5">{[...Array(5)].map((_,i)=><div key={i} className="rounded-2xl border border-slate-200 bg-white p-5 animate-pulse"><div className="h-7 w-12 rounded bg-slate-200 mb-2"/><div className="h-3 w-20 rounded bg-slate-100"/></div>)}</div>
    </div>
  );
  if (error && !dashboard) return <div className="rounded-2xl border border-rose-200 bg-rose-50 p-6 text-rose-700">{error}</div>;

  const jobs = Array.isArray(dashboard?.jobs) ? dashboard.jobs : [];
  const totalPipeline = (dashboard?.appliedCount||0)+(dashboard?.shortlistedCount||0)+(dashboard?.interviewScheduledCount||0)+(dashboard?.offeredCount||0);
  const conversionRate = totalPipeline ? Math.round(((dashboard?.offeredCount||0)*1000)/totalPipeline)/10 : 0;

  const kpis = [
    {icon:"📋",label:"Active Jobs",val:dashboard?.activeJobs||0,bg:"#fff",border:"#e2e8f0",color:"#0f172a"},
    {icon:"📨",label:"Applied",val:dashboard?.appliedCount||0,bg:"#eff6ff",border:"#bfdbfe",color:"#1e3a5f"},
    {icon:"⭐",label:"Shortlisted",val:dashboard?.shortlistedCount||0,bg:"#fffbeb",border:"#fde68a",color:"#78350f"},
    {icon:"🗓️",label:"Interviews",val:dashboard?.interviewScheduledCount||0,bg:"#f0fdf4",border:"#bbf7d0",color:"#065f46"},
    {icon:"🎯",label:"Offer Rate",val:`${conversionRate}%`,bg:"#f0fdfa",border:"#99f6e4",color:"#134e4a"},
  ];

  return (
    <div className="space-y-6">
      <section className="portal-banner">
        <p style={{color:"#cffafe",fontSize:11,fontWeight:700,textTransform:"uppercase",letterSpacing:"0.2em"}}>Recruiter Workspace</p>
        <h2 style={{color:"#fff",fontSize:28,fontWeight:800,marginTop:6,fontFamily:"var(--font-display)"}}>Hiring Command Center</h2>
        <p style={{color:"rgba(255,255,255,0.75)",fontSize:13,marginTop:4}}>Post roles, manage pipelines, and track offer conversions in real-time.</p>
        {message && <div style={{marginTop:12,display:"inline-block",background:"rgba(52,211,153,0.2)",border:"1px solid rgba(52,211,153,0.4)",borderRadius:8,padding:"5px 14px",color:"#34d399",fontSize:12,fontWeight:600}}>✓ {message}</div>}
      </section>

      {/* KPI cards */}
      <div className="grid gap-3 md:grid-cols-5">
        {kpis.map(k=>(
          <div key={k.label} style={{background:k.bg,border:`1px solid ${k.border}`,borderRadius:20,padding:"18px 20px",transition:"all 0.2s",cursor:"default"}}
            onMouseEnter={e=>{e.currentTarget.style.transform="translateY(-3px)";e.currentTarget.style.boxShadow="0 8px 24px rgba(0,0,0,0.1)";}}
            onMouseLeave={e=>{e.currentTarget.style.transform="none";e.currentTarget.style.boxShadow="none";}}>
            <div style={{fontSize:26,marginBottom:8}}>{k.icon}</div>
            <p style={{fontSize:26,fontWeight:900,color:k.color,lineHeight:1,fontFamily:"var(--font-display)"}}>{k.val}</p>
            <p style={{fontSize:10,fontWeight:700,color:"#94a3b8",textTransform:"uppercase",letterSpacing:"0.08em",marginTop:4}}>{k.label}</p>
          </div>
        ))}
      </div>

      {/* Main 2-col layout */}
      <div className="grid gap-5 xl:grid-cols-[2fr_1fr]">
        {/* Post New Role */}
        <div style={{background:"#fff",border:"1px solid #e2e8f0",borderRadius:20,padding:24}}>
          <div style={{display:"flex",alignItems:"center",gap:12,marginBottom:20}}>
            <div style={{width:44,height:44,borderRadius:13,background:"linear-gradient(135deg,#0f766e,#0d9488)",display:"grid",placeItems:"center",fontSize:20,flexShrink:0}}>➕</div>
            <div>
              <h3 style={{color:"#0f172a",fontWeight:800,fontSize:17,margin:0,fontFamily:"var(--font-display)"}}>Post New Role</h3>
              <p style={{color:"#64748b",fontSize:12,margin:0}}>Notify all eligible students automatically</p>
            </div>
          </div>
          <form onSubmit={createJob} className="space-y-3">
            <div className="grid gap-3 md:grid-cols-2">
              {[
                {key:"title",ph:"Job title *"},
                {key:"location",ph:"Location"},
                {key:"compensation",ph:"Compensation (e.g. 8-12 LPA)"},
                {key:"minCgpa",ph:"Min CGPA",type:"number",step:"0.1",min:"0",max:"10"},
              ].map(f=>(
                <input key={f.key} type={f.type||"text"} step={f.step} min={f.min} max={f.max}
                  className="rounded-xl border border-slate-200 bg-slate-50 px-3 py-2.5 text-sm focus:border-teal-500 focus:outline-none transition"
                  style={{color:"#0f172a"}} placeholder={f.ph} value={form[f.key]}
                  onChange={e=>setForm(p=>({...p,[f.key]:e.target.value}))} />
              ))}
              <select className="rounded-xl border border-slate-200 bg-slate-50 px-3 py-2.5 text-sm focus:border-teal-500 focus:outline-none"
                style={{color:"#0f172a"}} value={form.jobType} onChange={e=>setForm(p=>({...p,jobType:e.target.value}))}>
                <option value="FULL_TIME">Full Time</option>
                <option value="INTERNSHIP">Internship</option>
                <option value="CONTRACT">Contract</option>
              </select>
              <input className="rounded-xl border border-slate-200 bg-slate-50 px-3 py-2.5 text-sm focus:border-teal-500 focus:outline-none"
                style={{color:"#0f172a"}} placeholder="Required skills (e.g. Java, Python)" value={form.requiredSkills}
                onChange={e=>setForm(p=>({...p,requiredSkills:e.target.value}))} />
              <textarea rows={3} className="rounded-xl border border-slate-200 bg-slate-50 px-3 py-2.5 text-sm md:col-span-2 focus:border-teal-500 focus:outline-none"
                style={{color:"#0f172a",resize:"vertical"}} placeholder="Role description..."
                value={form.description} onChange={e=>setForm(p=>({...p,description:e.target.value}))} />
            </div>
            {error && <p style={{color:"#dc2626",fontSize:13}}>⚠ {error}</p>}
            <button type="submit" disabled={creating}
              style={{background:creating?"#94a3b8":"linear-gradient(135deg,#0f766e,#0d9488)",color:"#fff",border:"none",borderRadius:12,padding:"11px 24px",fontWeight:700,fontSize:14,cursor:creating?"not-allowed":"pointer",boxShadow:"0 4px 12px rgba(15,118,110,0.3)"}}>
              {creating ? "⏳ Posting..." : "🚀 Post Job Opening"}
            </button>
          </form>
        </div>

        {/* Notifications panel */}
        <NotificationPanel notifications={notifications} markRead={markRead} loading={notificationsLoading} />
      </div>

      {/* Job openings */}
      <div>
        <h3 style={{color:"#0f172a",fontWeight:800,fontSize:18,marginBottom:14,fontFamily:"var(--font-display)"}}>
          Your Openings <span style={{color:"#94a3b8",fontWeight:500,fontSize:15}}>({jobs.length})</span>
        </h3>
        {jobs.length===0 && (
          <div style={{background:"#f8fafc",border:"2px dashed #e2e8f0",borderRadius:20,padding:40,textAlign:"center"}}>
            <p style={{fontSize:36,marginBottom:8}}>📭</p>
            <p style={{color:"#64748b",fontSize:14,margin:0}}>No jobs posted yet. Create your first opening above.</p>
          </div>
        )}
        <div className="space-y-3">
          {jobs.map(job=>{
            const insight=jobInsights[job.jobId]||{total:0,counts:{}};
            return (
              <div key={job.jobId} style={{background:"#fff",border:"1px solid #e2e8f0",borderRadius:20,padding:20,transition:"all 0.2s"}}
                onMouseEnter={e=>{e.currentTarget.style.boxShadow="0 4px 16px rgba(0,0,0,0.08)";e.currentTarget.style.borderColor="#cbd5e1";}}
                onMouseLeave={e=>{e.currentTarget.style.boxShadow="none";e.currentTarget.style.borderColor="#e2e8f0";}}>
                <div style={{display:"flex",justifyContent:"space-between",alignItems:"flex-start",flexWrap:"wrap",gap:12,marginBottom:10}}>
                  <div>
                    <p style={{fontWeight:800,fontSize:16,color:"#0f172a",margin:0,fontFamily:"var(--font-display)"}}>{job.title}</p>
                    <p style={{color:"#64748b",fontSize:13,margin:"3px 0 0"}}>📍 {job.location} · {job.jobType} · Min CGPA {job.minCgpa??"—"}</p>
                    <p style={{color:"#94a3b8",fontSize:11,marginTop:2}}>Posted {formatDateTime(job.createdAt)}</p>
                  </div>
                  <button onClick={()=>navigate(`/recruiter/monitoring?jobId=${job.jobId}`)}
                    style={{background:"#0f766e",color:"#fff",border:"none",borderRadius:12,padding:"9px 18px",fontWeight:700,fontSize:13,cursor:"pointer",whiteSpace:"nowrap"}}
                    onMouseEnter={e=>e.target.style.background="#0d9488"}
                    onMouseLeave={e=>e.target.style.background="#0f766e"}>
                    Manage Pipeline →
                  </button>
                </div>
                {job.description && <p style={{color:"#475569",fontSize:13,marginBottom:12,lineClamp:2}}>{job.description.slice(0,140)}{job.description.length>140?"...":""}</p>}
                <div style={{display:"grid",gridTemplateColumns:"repeat(4,1fr)",gap:8}}>
                  {[["📨","Applicants",insight.total,"#eff6ff","#1e3a5f","#bfdbfe"],
                    ["⭐","Shortlisted",insight.counts.SHORTLISTED||0,"#fffbeb","#78350f","#fde68a"],
                    ["🗓️","Interviews",insight.counts.INTERVIEW_SCHEDULED||0,"#f0fdf4","#065f46","#bbf7d0"],
                    ["🎯","Offers",insight.counts.OFFERED||0,"#f0fdfa","#134e4a","#99f6e4"],
                  ].map(([icon,label,val,bg,color,border])=>(
                    <div key={label} style={{background:bg,border:`1px solid ${border}`,borderRadius:12,padding:"10px 12px",textAlign:"center"}}>
                      <p style={{fontSize:16,margin:0}}>{icon}</p>
                      <p style={{fontWeight:900,fontSize:20,color,margin:"3px 0 0",fontFamily:"var(--font-display)"}}>{val}</p>
                      <p style={{fontSize:10,color:"#94a3b8",fontWeight:700,textTransform:"uppercase",margin:0}}>{label}</p>
                    </div>
                  ))}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};
export const RecruiterTopStudentsPage = () => {
  const [top, setTop] = useState({});
  const [selectedStudentId, setSelectedStudentId] = useState(null);
  const [details, setDetails] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [shortlist, setShortlist] = useState(() => readPersistentList("recruiter_shortlist_v2"));

  useEffect(() => {
    localStorage.setItem("recruiter_shortlist_v2", JSON.stringify(shortlist));
  }, [shortlist]);

  useEffect(() => {
    let active = true;
    api.get("/top-students")
      .then((res) => {
        if (active) setTop(res.data?.topByDepartment || {});
      })
      .catch((e) => {
        if (active) setError(e.response?.data?.message || "Failed to load top student pools.");
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  const viewCandidate = async (studentId) => {
    setSelectedStudentId(studentId);
    setError("");
    try {
      const { data } = await api.get(`/student-details?studentId=${studentId}`);
      setDetails(data);
    } catch (e) {
      setDetails(null);
      setError(e.response?.data?.message || "Failed to load candidate details.");
    }
  };

  const addToShortlist = () => {
    if (!details) return;
    if (shortlist.some((item) => item.studentId === details.studentId)) return;
    const fitScore = Math.round((Number(details.readiness || 0) * 0.45) + (Number(details.coding || 0) * 0.25) + (Number(details.softSkills || 0) * 0.3));
    startTransition(() => {
      setShortlist((prev) => [{ ...details, fitScore }, ...prev]);
    });
  };

  if (loading) return <p>Loading talent pools...</p>;

  return (
    <div className="space-y-5">
      <section className="portal-banner">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-100">Recruiter Workspace</p>
        <h2 className="mt-2 text-3xl font-bold text-white">Top Talent Pools</h2>
        <p className="mt-2 text-sm text-cyan-50/90">Review leaderboard candidates department-wise and build your shortlist with fit scoring.</p>
      </section>

      {error ? <p className="text-sm text-rose-600">{error}</p> : null}

      <div className="grid gap-4 xl:grid-cols-[2fr_1fr]">
        <div className="grid gap-4 lg:grid-cols-2">
          {Object.entries(top).map(([department, students]) => (
            <div key={department} className="dashboard-card p-4">
              <h3 className="font-semibold text-slate-900">{department}</h3>
              <ul className="mt-3 space-y-2 text-sm text-slate-700">
                {(students || []).map((student, index) => (
                  <li key={student.studentId} className="flex items-center justify-between gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2">
                    <div>
                      <p className="font-semibold">#{index + 1} {student.name}</p>
                      <p className="text-xs text-slate-500">Readiness {student.readiness}% | Final {student.finalScore}%</p>
                    </div>
                    <button type="button" className={`rounded-lg border px-2 py-1 text-xs ${selectedStudentId === student.studentId ? "border-cyan-400 text-cyan-800" : "border-slate-300 text-slate-700"}`} onClick={() => viewCandidate(student.studentId)}>
                      View
                    </button>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        <div className="space-y-4">
          <div className="dashboard-card p-4">
            <h3 className="font-semibold text-slate-900">Candidate Snapshot</h3>
            {!details ? <p className="mt-2 text-sm text-slate-600">Select a candidate from a department leaderboard.</p> : (
              <div className="mt-3 space-y-2 text-sm text-slate-700">
                <p className="text-base font-semibold text-slate-900">{details.name}</p>
                <p>{details.department}</p>
                <p>Readiness: {details.readiness}%</p>
                <p>Coding: {details.coding}%</p>
                <p>Aptitude: {details.aptitude}%</p>
                <p>Soft Skills: {details.softSkills}%</p>
                <p>Weak Areas: {(details.weakAreas || []).join(", ")}</p>
                <button type="button" className="mt-2 rounded-xl bg-slate-900 px-3 py-2 text-sm font-semibold text-white" onClick={addToShortlist}>Add to Shortlist</button>
              </div>
            )}
          </div>

          <div className="dashboard-card p-4">
            <h3 className="font-semibold text-slate-900">Saved Shortlist</h3>
            <ul className="mt-2 space-y-2 text-sm text-slate-700">
              {shortlist.length === 0 ? <li>No shortlisted candidates yet.</li> : null}
              {shortlist.map((candidate) => (
                <li key={candidate.studentId} className="rounded-xl border border-slate-200 bg-white px-3 py-2">
                  <p className="font-semibold text-slate-900">{candidate.name}</p>
                  <p>{candidate.department}</p>
                  <p className="text-xs text-slate-500">Fit score: {candidate.fitScore}%</p>
                </li>
              ))}
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};
export const RecruiterMonitoringPage = () => {
  const location = useLocation();
  const [dashboard, setDashboard] = useState(null);
  const [interviewers, setInterviewers] = useState([]);
  const [applications, setApplications] = useState([]);
  const [selectedJobId, setSelectedJobId] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [statusDrafts, setStatusDrafts] = useState({});
  const [scheduleDrafts, setScheduleDrafts] = useState({});
  const [loading, setLoading] = useState(true);
  const [applicationsLoading, setApplicationsLoading] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const { notifications, loading: notificationsLoading, markRead, refresh: refreshNotifications } = usePortalNotifications();

  const selectedJob = useMemo(
    () => (dashboard?.jobs || []).find((job) => String(job.jobId) === String(selectedJobId)),
    [dashboard, selectedJobId],
  );

  const loadBaseData = async () => {
    setLoading(true);
    setError("");
    try {
      const [{ data: recruiterData }, { data: interviewerData }] = await Promise.all([
        api.get("/api/portal/recruiter/dashboard"),
        api.get("/api/portal/recruiter/interviewers"),
      ]);
      const jobs = Array.isArray(recruiterData?.jobs) ? recruiterData.jobs : [];
      setDashboard(recruiterData);
      setInterviewers(Array.isArray(interviewerData) ? interviewerData : []);
      const queryJobId = new URLSearchParams(location.search).get("jobId");
      if (queryJobId && jobs.some((job) => String(job.jobId) === queryJobId)) {
        setSelectedJobId(queryJobId);
      } else if (!selectedJobId && jobs.length > 0) {
        setSelectedJobId(String(jobs[0].jobId));
      }
    } catch (e) {
      setError(e.response?.data?.message || "Failed to load monitoring data.");
    } finally {
      setLoading(false);
    }
  };

  const loadApplications = async (jobId, status) => {
    if (!jobId) {
      setApplications([]);
      return;
    }
    setApplicationsLoading(true);
    setError("");
    try {
      const params = { jobId: Number(jobId) };
      if (status && status !== "ALL") params.status = status;
      const { data } = await api.get("/api/portal/recruiter/applications", { params });
      const list = Array.isArray(data) ? data : [];
      setApplications(list);
      setStatusDrafts((prev) => {
        const next = { ...prev };
        list.forEach((item) => {
          if (!next[item.applicationId]) {
            next[item.applicationId] = { status: item.status || "APPLIED", recruiterNotes: "" };
          }
        });
        return next;
      });
      setScheduleDrafts((prev) => {
        const next = { ...prev };
        list.forEach((item) => {
          if (!next[item.applicationId]) {
            next[item.applicationId] = {
              interviewerUserId: interviewers[0]?.userId ? String(interviewers[0].userId) : "",
              scheduledAt: "",
              durationMinutes: 45,
              mode: "ONLINE",
              meetingLink: "",
            };
          }
        });
        return next;
      });
    } catch (e) {
      setError(e.response?.data?.message || "Failed to load applications.");
      setApplications([]);
    } finally {
      setApplicationsLoading(false);
    }
  };

  useEffect(() => {
    loadBaseData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    loadApplications(selectedJobId, statusFilter);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedJobId, statusFilter, interviewers.length]);

  const updateStatus = async (applicationId) => {
    const draft = statusDrafts[applicationId];
    if (!draft?.status) return;
    setError("");
    setMessage("");
    try {
      await api.patch(`/api/portal/recruiter/applications/${applicationId}/status`, draft);
      setMessage(`Application #${applicationId} updated.`);
      await Promise.all([
        loadApplications(selectedJobId, statusFilter),
        loadBaseData(),
        refreshNotifications(),
      ]);
    } catch (e) {
      setError(e.response?.data?.message || "Failed to update application status.");
    }
  };

  const scheduleInterview = async (applicationId) => {
    const draft = scheduleDrafts[applicationId];
    if (!draft?.interviewerUserId || !draft?.scheduledAt) {
      setError("Interviewer and schedule date-time are required.");
      return;
    }
    setError("");
    setMessage("");
    try {
      await api.post("/api/portal/recruiter/interviews", {
        jobApplicationId: applicationId,
        interviewerUserId: Number(draft.interviewerUserId),
        scheduledAt: draft.scheduledAt,
        durationMinutes: Number(draft.durationMinutes || 45),
        mode: draft.mode,
        meetingLink: draft.meetingLink || null,
      });
      setMessage(`Interview scheduled for application #${applicationId}.`);
      await Promise.all([
        loadApplications(selectedJobId, statusFilter),
        loadBaseData(),
        refreshNotifications(),
      ]);
    } catch (e) {
      setError(e.response?.data?.message || "Failed to schedule interview.");
    }
  };

  if (loading) return <p>Loading recruiter monitoring...</p>;
  const jobs = Array.isArray(dashboard?.jobs) ? dashboard.jobs : [];

  return (
    <div className="space-y-5">
      <section className="portal-banner">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-100">Recruiter Workspace</p>
        <h2 className="mt-2 text-3xl font-bold text-white">Pipeline Monitoring</h2>
        <p className="mt-2 text-sm text-cyan-50/90">Move candidates across statuses, schedule interviews, and keep the hiring funnel in control.</p>
      </section>

      <div className="dashboard-card grid gap-3 p-4 md:grid-cols-3">
        <select className="rounded-xl border border-slate-200 px-3 py-2 text-sm" value={selectedJobId} onChange={(e) => setSelectedJobId(e.target.value)}>
          <option value="">Select job opening</option>
          {jobs.map((job) => (
            <option key={job.jobId} value={job.jobId}>{job.title} ({job.jobType})</option>
          ))}
        </select>
        <select className="rounded-xl border border-slate-200 px-3 py-2 text-sm" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
          {APPLICATION_STATUSES.map((status) => <option key={status} value={status}>{status}</option>)}
        </select>
        <button type="button" className="rounded-xl border border-slate-300 px-3 py-2 text-sm font-semibold text-slate-800" onClick={() => loadApplications(selectedJobId, statusFilter)}>
          Refresh Pipeline
        </button>
      </div>

      {selectedJob ? (
        <div className="dashboard-card p-4 text-sm text-slate-700">
          <p className="text-base font-semibold text-slate-900">{selectedJob.title}</p>
          <p>{selectedJob.location} | {selectedJob.jobType} | Min CGPA {selectedJob.minCgpa ?? "-"}</p>
          <p className="mt-1 text-xs text-slate-500">{selectedJob.description}</p>
        </div>
      ) : null}

      {error ? <p className="text-sm text-rose-600">{error}</p> : null}
      {message ? <p className="text-sm text-emerald-700">{message}</p> : null}

      <div className="grid gap-4 xl:grid-cols-[2fr_1fr]">
        <div className="space-y-3">
          {applicationsLoading ? <p>Loading applications...</p> : null}
          {!applicationsLoading && applications.length === 0 ? <div className="dashboard-card p-4 text-sm text-slate-600">No applications found for the selected filters.</div> : null}
          {applications.map((application) => {
            const statusDraft = statusDrafts[application.applicationId] || { status: application.status || "APPLIED", recruiterNotes: "" };
            const scheduleDraft = scheduleDrafts[application.applicationId] || {
              interviewerUserId: interviewers[0]?.userId ? String(interviewers[0].userId) : "",
              scheduledAt: "",
              durationMinutes: 45,
              mode: "ONLINE",
              meetingLink: "",
            };
            return (
              <div key={application.applicationId} className="dashboard-card space-y-3 p-4">
                <div className="flex flex-wrap items-start justify-between gap-2">
                  <div>
                    <p className="font-semibold text-slate-900">{application.jobTitle}</p>
                    <p className="text-sm text-slate-600">{application.companyName}</p>
                    <p className="text-xs text-slate-500">Applied: {formatDateTime(application.appliedAt)} | Interview: {formatDateTime(application.interviewAt)}</p>
                  </div>
                  <span className={`rounded-full px-2 py-1 text-xs font-semibold ${statusClass(application.status)}`}>{application.status}</span>
                </div>

                <div className="grid gap-2 md:grid-cols-[1fr_1fr_auto]">
                  <select
                    className="rounded-xl border border-slate-200 px-3 py-2 text-sm"
                    value={statusDraft.status}
                    onChange={(e) => setStatusDrafts((prev) => ({ ...prev, [application.applicationId]: { ...statusDraft, status: e.target.value } }))}
                  >
                    {APPLICATION_STATUSES.filter((status) => status !== "ALL").map((status) => <option key={status} value={status}>{status}</option>)}
                  </select>
                  <input
                    className="rounded-xl border border-slate-200 px-3 py-2 text-sm"
                    placeholder="Recruiter notes"
                    value={statusDraft.recruiterNotes || ""}
                    onChange={(e) => setStatusDrafts((prev) => ({ ...prev, [application.applicationId]: { ...statusDraft, recruiterNotes: e.target.value } }))}
                  />
                  <button type="button" className="rounded-xl bg-slate-900 px-3 py-2 text-sm font-semibold text-white" onClick={() => updateStatus(application.applicationId)}>
                    Save Status
                  </button>
                </div>

                <div className="rounded-xl border border-slate-200 bg-slate-50 p-3">
                  <p className="text-xs font-semibold uppercase tracking-wide text-slate-600">Schedule Interview</p>
                  <div className="mt-2 grid gap-2 md:grid-cols-2">
                    <select
                      className="rounded-xl border border-slate-200 px-3 py-2 text-sm"
                      value={scheduleDraft.interviewerUserId}
                      onChange={(e) => setScheduleDrafts((prev) => ({ ...prev, [application.applicationId]: { ...scheduleDraft, interviewerUserId: e.target.value } }))}
                    >
                      <option value="">Select interviewer</option>
                      {interviewers.map((item) => <option key={item.userId} value={item.userId}>{item.name} ({item.email})</option>)}
                    </select>
                    <input
                      className="rounded-xl border border-slate-200 px-3 py-2 text-sm"
                      type="datetime-local"
                      value={scheduleDraft.scheduledAt}
                      onChange={(e) => setScheduleDrafts((prev) => ({ ...prev, [application.applicationId]: { ...scheduleDraft, scheduledAt: e.target.value } }))}
                    />
                    <select
                      className="rounded-xl border border-slate-200 px-3 py-2 text-sm"
                      value={scheduleDraft.mode}
                      onChange={(e) => setScheduleDrafts((prev) => ({ ...prev, [application.applicationId]: { ...scheduleDraft, mode: e.target.value } }))}
                    >
                      {INTERVIEW_MODES.map((mode) => <option key={mode} value={mode}>{mode}</option>)}
                    </select>
                    <input
                      className="rounded-xl border border-slate-200 px-3 py-2 text-sm"
                      type="number"
                      min="15"
                      value={scheduleDraft.durationMinutes}
                      onChange={(e) => setScheduleDrafts((prev) => ({ ...prev, [application.applicationId]: { ...scheduleDraft, durationMinutes: e.target.value } }))}
                      placeholder="Duration minutes"
                    />
                    <input
                      className="rounded-xl border border-slate-200 px-3 py-2 text-sm md:col-span-2"
                      placeholder="Meeting link (optional)"
                      value={scheduleDraft.meetingLink}
                      onChange={(e) => setScheduleDrafts((prev) => ({ ...prev, [application.applicationId]: { ...scheduleDraft, meetingLink: e.target.value } }))}
                    />
                  </div>
                  <button type="button" className="mt-2 rounded-xl bg-teal-700 px-3 py-2 text-sm font-semibold text-white" onClick={() => scheduleInterview(application.applicationId)}>
                    Schedule Interview
                  </button>
                </div>
              </div>
            );
          })}
        </div>

        <NotificationPanel notifications={notifications} markRead={markRead} loading={notificationsLoading} />
      </div>
    </div>
  );
};
export const StaffDashboardPage = () => {
  const [stats, setStats] = useState(null);
  const [top, setTop] = useState({});
  const [selectedStudentId, setSelectedStudentId] = useState("1");
  const [details, setDetails] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [exportMessage, setExportMessage] = useState("");
  const { notifications, loading: notificationsLoading, markRead } = usePortalNotifications();

  useEffect(() => {
    let active = true;
    Promise.all([api.get("/api/staff/department-stats"), api.get("/api/staff/top-students")])
      .then(([statsRes, topRes]) => {
        if (!active) return;
        setStats(statsRes.data);
        setTop(topRes.data?.topByDepartment || {});
      })
      .catch((e) => {
        if (active) setError(e.response?.data?.message || "Failed to load staff analytics.");
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  const lookupStudent = async () => {
    if (!selectedStudentId) return;
    setError("");
    try {
      const { data } = await api.get(`/student-details?studentId=${selectedStudentId}`);
      setDetails(data);
    } catch (e) {
      setDetails(null);
      setError(e.response?.data?.message || "Student not found.");
    }
  };

  if (loading) return (
    <div className="space-y-4">
      <div className="portal-banner animate-pulse"><div className="h-6 w-48 rounded bg-white/20 mb-2"/><div className="h-8 w-80 rounded bg-white/20"/></div>
      <div className="grid gap-4 md:grid-cols-5">{[...Array(5)].map((_,i)=><div key={i} className="rounded-2xl border border-slate-200 bg-white p-5 animate-pulse"><div className="h-7 w-12 rounded bg-slate-200 mb-2"/><div className="h-3 w-20 rounded bg-slate-100"/></div>)}</div>
    </div>
  );
  if (error && !stats) return <div className="rounded-2xl border border-rose-200 bg-rose-50 p-6" style={{color:"#dc2626"}}>{error}</div>;

  const departmentPerformance = Array.isArray(stats?.departmentPerformance) ? stats.departmentPerformance : [];
  const departmentsAtRisk = departmentPerformance.filter((item) => Number(item.averagePerformance || 0) < 60).length;
  const topStudents = flattenTopStudents(top);

  const exportDepartmentCsv = () => {
    downloadCsv("staff-department-readiness.csv", ["department","averagePerformance"], departmentPerformance.map(item=>[item.department,item.averagePerformance]));
  };
  const exportTopStudentsCsv = () => exportStudentsCsv("staff-top-students.csv", topStudents);
  const saveSnapshotToFirebase = async () => {
    setExportMessage("");
    try {
      const result = await saveFirebaseRecord(staffStorageKey("dashboardSnapshot"), { stats, topByDepartment: top });
      setExportMessage(result.saved ? "Snapshot saved to Firebase." : "Firebase not configured. CSV still works.");
    } catch (e) { setExportMessage(e.message || "Firebase save failed."); }
  };

  const staffKpis = [
    {icon:"🎓",label:"Total Students",val:stats?.totalStudents||0,bg:"#fff",border:"#e2e8f0",color:"#0f172a"},
    {icon:"🟢",label:"Active Users",val:stats?.activeUsers||0,bg:"#f0fdf4",border:"#bbf7d0",color:"#065f46"},
    {icon:"📅",label:"Logged In Today",val:stats?.loggedInToday||0,bg:"#eff6ff",border:"#bfdbfe",color:"#1e3a5f"},
    {icon:"🏛️",label:"Departments",val:departmentPerformance.length,bg:"#faf5ff",border:"#e9d5ff",color:"#581c87"},
    {icon:"⚠️",label:"At Risk",val:departmentsAtRisk,bg:departmentsAtRisk>0?"#fff1f2":"#f8fafc",border:departmentsAtRisk>0?"#fecaca":"#e2e8f0",color:departmentsAtRisk>0?"#7f1d1d":"#0f172a"},
  ];

  return (
    <div className="space-y-6">
      <section className="portal-banner">
        <p style={{color:"#cffafe",fontSize:11,fontWeight:700,textTransform:"uppercase",letterSpacing:"0.2em"}}>Staff Workspace</p>
        <h2 style={{color:"#fff",fontSize:28,fontWeight:800,marginTop:6,fontFamily:"var(--font-display)"}}>Placement Operations Center</h2>
        <p style={{color:"rgba(255,255,255,0.75)",fontSize:13,marginTop:4}}>Monitor institutional readiness, track at-risk cohorts, and drive placement outcomes.</p>
        {exportMessage && <div style={{marginTop:12,display:"inline-block",background:"rgba(52,211,153,0.2)",border:"1px solid rgba(52,211,153,0.4)",borderRadius:8,padding:"5px 14px",color:"#34d399",fontSize:12,fontWeight:600}}>{exportMessage}</div>}
      </section>

      {/* KPI cards */}
      <div className="grid gap-3 md:grid-cols-5">
        {staffKpis.map(k=>(
          <div key={k.label} style={{background:k.bg,border:`1px solid ${k.border}`,borderRadius:20,padding:"18px 20px",transition:"all 0.2s"}}
            onMouseEnter={e=>{e.currentTarget.style.transform="translateY(-3px)";e.currentTarget.style.boxShadow="0 8px 24px rgba(0,0,0,0.08)";}}
            onMouseLeave={e=>{e.currentTarget.style.transform="none";e.currentTarget.style.boxShadow="none";}}>
            <div style={{fontSize:26,marginBottom:8}}>{k.icon}</div>
            <p style={{fontSize:26,fontWeight:900,color:k.color,lineHeight:1,fontFamily:"var(--font-display)"}}>{k.val}</p>
            <p style={{fontSize:10,fontWeight:700,color:"#94a3b8",textTransform:"uppercase",letterSpacing:"0.08em",marginTop:4}}>{k.label}</p>
          </div>
        ))}
      </div>

      {/* Export toolbar */}
      <div style={{background:"#fff",border:"1px solid #e2e8f0",borderRadius:16,padding:"14px 20px",display:"flex",flexWrap:"wrap",alignItems:"center",gap:10}}>
        <span style={{fontSize:13,fontWeight:700,color:"#0f172a",marginRight:4}}>📥 Export:</span>
        {[
          {label:"Department CSV",fn:exportDepartmentCsv,bg:"#0f766e"},
          {label:"Top Students CSV",fn:exportTopStudentsCsv,bg:"#0369a1"},
          {label:"Firebase Snapshot",fn:saveSnapshotToFirebase,bg:"#7c3aed"},
        ].map(b=>(
          <button key={b.label} onClick={b.fn}
            style={{background:b.bg,color:"#fff",border:"none",borderRadius:10,padding:"7px 16px",fontWeight:600,fontSize:13,cursor:"pointer"}}
            onMouseEnter={e=>e.target.style.opacity="0.85"}
            onMouseLeave={e=>e.target.style.opacity="1"}>
            {b.label}
          </button>
        ))}
        <span style={{fontSize:11,color:"#94a3b8"}}>{isFirebaseConfigured() ? "✓ Firebase configured" : "Firebase not configured"}</span>
      </div>

      {/* Charts + notifications */}
      <div className="grid gap-5 xl:grid-cols-[2fr_1fr]">
        <div style={{background:"#fff",border:"1px solid #e2e8f0",borderRadius:20,padding:20}}>
          <p style={{fontWeight:800,fontSize:16,color:"#0f172a",marginBottom:16,fontFamily:"var(--font-display)"}}>📊 Department Readiness Heatmap</p>
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={departmentPerformance} margin={{top:5,right:10,left:0,bottom:40}}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
              <XAxis dataKey="department" tick={{fontSize:11,fill:"#64748b"}} angle={-30} textAnchor="end" interval={0} />
              <YAxis tick={{fontSize:11,fill:"#64748b"}} domain={[0,100]} />
              <Tooltip contentStyle={{borderRadius:12,border:"1px solid #e2e8f0",background:"#fff",color:"#0f172a"}} formatter={(v)=>[`${v}%`,"Avg Readiness"]} />
              <Bar dataKey="averagePerformance" radius={[6,6,0,0]} fill="url(#tealGrad)" />
              <defs>
                <linearGradient id="tealGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="#0d9488"/>
                  <stop offset="100%" stopColor="#0f766e" stopOpacity={0.7}/>
                </linearGradient>
              </defs>
            </BarChart>
          </ResponsiveContainer>
        </div>
        <div className="space-y-4">
          <NotificationPanel notifications={notifications} markRead={markRead} loading={notificationsLoading} />
        </div>
      </div>

      {/* Top students + student lookup */}
      <div className="grid gap-5 lg:grid-cols-2">
        <div style={{background:"#fff",border:"1px solid #e2e8f0",borderRadius:20,padding:20}}>
          <p style={{fontWeight:800,fontSize:16,color:"#0f172a",marginBottom:12,fontFamily:"var(--font-display)"}}>🏅 Department Leaders</p>
          <div className="space-y-3">
            {Object.entries(top).map(([dept, students])=>(
              <div key={dept} style={{background:"#f8fafc",border:"1px solid #e2e8f0",borderRadius:14,padding:"12px 16px"}}>
                <p style={{fontWeight:700,fontSize:13,color:"#0f172a",marginBottom:8}}>🏛️ {dept}</p>
                <div className="space-y-2">
                  {(students||[]).slice(0,3).map((s,i)=>(
                    <div key={s.studentId} style={{display:"flex",alignItems:"center",justifyContent:"space-between",background:"#fff",border:"1px solid #f1f5f9",borderRadius:10,padding:"8px 12px"}}>
                      <div style={{display:"flex",alignItems:"center",gap:10}}>
                        <span style={{width:22,height:22,borderRadius:"50%",background:i===0?"#fbbf24":i===1?"#94a3b8":"#cd7f32",display:"grid",placeItems:"center",fontSize:11,fontWeight:900,color:"#fff"}}>{i+1}</span>
                        <div>
                          <p style={{fontWeight:700,fontSize:13,color:"#0f172a",margin:0}}>{s.name}</p>
                          <p style={{fontSize:11,color:"#64748b",margin:0}}>Readiness: {s.readiness}%</p>
                        </div>
                      </div>
                      <button onClick={()=>{ setSelectedStudentId(String(s.studentId)); setTimeout(lookupStudent,100); }}
                        style={{fontSize:11,fontWeight:600,color:"#0f766e",background:"#f0fdfa",border:"1px solid #99f6e4",borderRadius:8,padding:"3px 10px",cursor:"pointer"}}>
                        View
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>

        <div style={{background:"#fff",border:"1px solid #e2e8f0",borderRadius:20,padding:20}}>
          <p style={{fontWeight:800,fontSize:16,color:"#0f172a",marginBottom:12,fontFamily:"var(--font-display)"}}>🔍 Student Lookup</p>
          <div style={{display:"flex",gap:8,marginBottom:14}}>
            <input value={selectedStudentId} onChange={e=>setSelectedStudentId(e.target.value)}
              style={{flex:1,border:"1px solid #e2e8f0",borderRadius:10,padding:"8px 14px",fontSize:13,color:"#0f172a",outline:"none"}}
              placeholder="Enter Student ID" />
            <button onClick={lookupStudent}
              style={{background:"#0f766e",color:"#fff",border:"none",borderRadius:10,padding:"8px 16px",fontWeight:700,fontSize:13,cursor:"pointer"}}>
              Search
            </button>
          </div>
          {error && <p style={{color:"#dc2626",fontSize:13,marginBottom:8}}>⚠ {error}</p>}
          {details ? (
            <div style={{background:"#f8fafc",border:"1px solid #e2e8f0",borderRadius:14,padding:16}}>
              <p style={{fontWeight:800,fontSize:16,color:"#0f172a",margin:"0 0 8px"}}>{details.name}</p>
              <p style={{fontSize:13,color:"#475569",margin:"0 0 12px"}}>{details.department}</p>
              <div className="grid grid-cols-2 gap-2">
                {[["🎯","Readiness",`${details.readiness}%`,"#f0fdfa","#134e4a"],
                  ["📝","Aptitude",`${details.aptitude}%`,"#eff6ff","#1e3a5f"],
                  ["💻","Coding",`${details.coding}%`,"#f0fdf4","#065f46"],
                  ["🗣️","Soft Skills",`${details.softSkills}%`,"#faf5ff","#581c87"],
                ].map(([icon,lab,val,bg,color])=>(
                  <div key={lab} style={{background:bg,borderRadius:10,padding:"10px 12px",textAlign:"center"}}>
                    <p style={{fontSize:14,margin:0}}>{icon}</p>
                    <p style={{fontWeight:900,fontSize:18,color,margin:"2px 0 0",fontFamily:"var(--font-display)"}}>{val}</p>
                    <p style={{fontSize:10,color:"#94a3b8",fontWeight:700,textTransform:"uppercase",margin:0}}>{lab}</p>
                  </div>
                ))}
              </div>
              {Array.isArray(details.weakAreas) && details.weakAreas.length > 0 && (
                <div style={{marginTop:12,background:"#fff7ed",border:"1px solid #fed7aa",borderRadius:10,padding:"10px 14px"}}>
                  <p style={{fontSize:12,fontWeight:700,color:"#78350f",marginBottom:6}}>⚠️ Weak Areas</p>
                  <div style={{display:"flex",flexWrap:"wrap",gap:6}}>
                    {details.weakAreas.map(w=><span key={w} style={{background:"#fef3c7",border:"1px solid #fde68a",borderRadius:6,padding:"2px 10px",fontSize:11,color:"#78350f",fontWeight:600}}>{w}</span>)}
                  </div>
                </div>
              )}
            </div>
          ) : (
            <div style={{textAlign:"center",padding:"24px 0",color:"#94a3b8",fontSize:13}}>
              <p style={{fontSize:28,marginBottom:8}}>🔍</p>
              Enter a student ID to view their detailed profile.
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
export const StaffTalentPoolPage = () => {
  const [top, setTop] = useState({});
  const [query, setQuery] = useState("");
  const deferredQuery = useDeferredValue(query);
  const [candidate, setCandidate] = useState(null);
  const [error, setError] = useState("");
  const [saveMessage, setSaveMessage] = useState("");
  const [watchlist, setWatchlist] = useState(() => readPersistentList("staff_watchlist_v1"));

  useEffect(() => {
    localStorage.setItem("staff_watchlist_v1", JSON.stringify(watchlist));
    if (!isFirebaseConfigured()) return;
    saveFirebaseRecord(staffStorageKey("watchlist"), { watchlist })
      .then(() => setSaveMessage("Watchlist saved to Firebase."))
      .catch(() => setSaveMessage("Watchlist saved locally. Firebase sync failed."));
  }, [watchlist]);

  useEffect(() => {
    api.get("/api/staff/top-students")
      .then((res) => setTop(res.data?.topByDepartment || {}))
      .catch((e) => setError(e.response?.data?.message || "Failed to load talent pool."));
  }, []);

  const flattened = useMemo(() => flattenTopStudents(top), [top]);

  const filtered = useMemo(() => {
    if (!deferredQuery.trim()) return flattened;
    const key = deferredQuery.toLowerCase();
    return flattened.filter((item) => `${item.name} ${item.department}`.toLowerCase().includes(key));
  }, [flattened, deferredQuery]);

  const openCandidate = async (studentId) => {
    setError("");
    try {
      const { data } = await api.get(`/student-details?studentId=${studentId}`);
      setCandidate(data);
    } catch (e) {
      setCandidate(null);
      setError(e.response?.data?.message || "Failed to load student details.");
    }
  };

  const addWatch = () => {
    if (!candidate) return;
    if (watchlist.some((item) => item.studentId === candidate.studentId)) return;
    startTransition(() => {
      setWatchlist((prev) => [...prev, candidate]);
    });
  };

  const exportFilteredCsv = () => {
    exportStudentsCsv("staff-talent-pool.csv", filtered);
  };

  const exportWatchlistCsv = () => {
    downloadCsv(
      "staff-watchlist.csv",
      ["studentId", "name", "department", "readiness", "coding", "aptitude", "softSkills", "weakAreas"],
      watchlist.map((item) => [
        item.studentId,
        item.name,
        item.department,
        item.readiness ?? "",
        item.coding ?? "",
        item.aptitude ?? "",
        item.softSkills ?? "",
        (item.weakAreas || []).join("; "),
      ]),
    );
  };

  const saveTalentSnapshot = async () => {
    setSaveMessage("");
    try {
      const result = await saveFirebaseRecord(staffStorageKey("talentSnapshot"), {
        filtered,
        watchlist,
      });
      setSaveMessage(result.saved
        ? "Talent snapshot saved to Firebase."
        : "Firebase is not configured. CSV download still works.");
    } catch (e) {
      setSaveMessage(e.message || "Firebase save failed.");
    }
  };

  return (
    <div className="space-y-5">
      <section className="portal-banner">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-100">Staff Workspace</p>
        <h2 className="mt-2 text-3xl font-bold text-white">Talent Intelligence Board</h2>
        <p className="mt-2 text-sm text-cyan-50/90">Search top talent across departments, review performance signals, and maintain a staff watchlist.</p>
      </section>

      <div className="dashboard-card p-4">
        <input
          className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"
          placeholder="Search candidate or department"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
      </div>

      <div className="dashboard-card flex flex-wrap items-center gap-2 p-4">
        <button type="button" onClick={exportFilteredCsv} className="rounded-xl bg-slate-900 px-3 py-2 text-sm font-semibold text-white">
          Download Talent CSV
        </button>
        <button type="button" onClick={exportWatchlistCsv} className="rounded-xl bg-teal-700 px-3 py-2 text-sm font-semibold text-white">
          Download Watchlist CSV
        </button>
        <button type="button" onClick={saveTalentSnapshot} className="rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700">
          Save to Firebase
        </button>
        <span className="text-xs text-slate-500">
          {isFirebaseConfigured() ? "Firebase storage is configured." : "Firebase env is not configured; exports download locally."}
        </span>
        {saveMessage ? <p className="w-full text-sm text-slate-600">{saveMessage}</p> : null}
      </div>

      {error ? <p className="text-sm text-rose-600">{error}</p> : null}

      <div className="grid gap-4 xl:grid-cols-[2fr_1fr]">
        <div className="dashboard-card p-4">
          <h3 className="font-semibold text-slate-900">Top Candidate Stream</h3>
          <div className="mt-3 overflow-auto">
            <table className="min-w-full text-sm">
              <thead>
                <tr className="border-b border-slate-200 text-left text-slate-600">
                  <th className="px-2 py-2">Candidate</th>
                  <th className="px-2 py-2">Department</th>
                  <th className="px-2 py-2">Readiness</th>
                  <th className="px-2 py-2">Action</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((item) => (
                  <tr key={item.studentId} className="border-b border-slate-100 text-slate-700">
                    <td className="px-2 py-2">{item.name}</td>
                    <td className="px-2 py-2">{item.department}</td>
                    <td className="px-2 py-2">{item.readiness}%</td>
                    <td className="px-2 py-2">
                      <button type="button" className="rounded-lg border border-slate-300 px-2 py-1 text-xs" onClick={() => openCandidate(item.studentId)}>
                        Inspect
                      </button>
                    </td>
                  </tr>
                ))}
                {filtered.length === 0 ? (
                  <tr>
                    <td colSpan={4} className="px-2 py-4 text-center text-slate-500">No candidates found.</td>
                  </tr>
                ) : null}
              </tbody>
            </table>
          </div>
        </div>

        <div className="space-y-4">
          <div className="dashboard-card p-4">
            <h3 className="font-semibold text-slate-900 mb-3">🔍 Candidate Insight</h3>
            {!candidate ? (
              <div className="flex flex-col items-center justify-center py-8 text-center">
                <span className="text-4xl mb-2">👤</span>
                <p className="text-sm text-slate-500">Click Inspect on any candidate to view their profile.</p>
              </div>
            ) : (
              <div className="space-y-3">
                <div className="flex items-start gap-3">
                  <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-gradient-to-br from-teal-600 to-emerald-500 text-white font-bold text-lg flex-shrink-0">
                    {(candidate.name || "?")[0]}
                  </div>
                  <div>
                    <p className="font-bold text-slate-900 text-base">{candidate.name}</p>
                    <p className="text-xs text-slate-500">{candidate.department}</p>
                    <div className="mt-1"><RiskBadge readiness={candidate.readiness} /></div>
                  </div>
                </div>

                <div className="space-y-2 pt-1">
                  {[["Readiness", candidate.readiness, "from-teal-500 to-emerald-400"],
                    ["Aptitude", candidate.aptitude, "from-sky-500 to-blue-400"],
                    ["Coding", candidate.coding, "from-indigo-500 to-violet-400"],
                    ["Soft Skills", candidate.softSkills, "from-rose-400 to-pink-400"],
                  ].map(([label, val, color]) => (
                    <div key={label}>
                      <div className="mb-1 flex justify-between text-xs">
                        <span className="font-medium text-slate-600">{label}</span>
                        <span className="font-bold text-slate-800">{val || 0}%</span>
                      </div>
                      <div className="h-1.5 w-full rounded-full bg-slate-100 overflow-hidden">
                        <div className={`h-1.5 rounded-full bg-gradient-to-r ${color}`} style={{width: `${val||0}%`}} />
                      </div>
                    </div>
                  ))}
                </div>

                {Array.isArray(candidate.weakAreas) && candidate.weakAreas.length > 0 && (
                  <div className="rounded-xl bg-amber-50 border border-amber-100 p-3">
                    <p className="text-xs font-bold text-amber-700 mb-2">⚠️ Weak Areas</p>
                    <div className="flex flex-wrap gap-1.5">
                      {candidate.weakAreas.map(w => (
                        <span key={w} className="rounded-full bg-amber-100 text-amber-800 px-2 py-0.5 text-[10px] font-semibold">{w}</span>
                      ))}
                    </div>
                  </div>
                )}
                <button type="button" className="w-full rounded-xl bg-slate-900 px-3 py-2 text-sm font-semibold text-white hover:bg-slate-700 transition" onClick={addWatch}>
                  ➕ Add to Watchlist
                </button>
              </div>
            )}
          </div>

          <div className="dashboard-card p-4">
            <h3 className="font-semibold text-slate-900 mb-3">📄 Staff Watchlist</h3>
            {watchlist.length === 0 ? (
              <p className="text-sm text-slate-500 text-center py-4">No students in watchlist yet.</p>
            ) : (
              <ul className="space-y-2">
                {watchlist.map((item) => (
                  <li key={item.studentId} className="flex items-center justify-between rounded-xl border border-slate-200 bg-slate-50 px-3 py-2">
                    <div>
                      <p className="font-semibold text-slate-900 text-sm">{item.name}</p>
                      <p className="text-xs text-slate-500">{item.department} · {item.readiness}% ready</p>
                    </div>
                    <RiskBadge readiness={item.readiness} />
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export const StaffMonitoringPage = () => {
  const [studentId, setStudentId] = useState("1");
  const [details, setDetails] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const inspect = async () => {
    if (!studentId) return;
    setLoading(true);
    setError("");
    try {
      const { data } = await api.get(`/student-details?studentId=${studentId}`);
      setDetails(data);
    } catch (e) {
      setDetails(null);
      setError(e.response?.data?.message || "Student not found.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    inspect();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const activity = Array.isArray(details?.activity) ? details.activity : [];
  const trendData = activity
    .slice(0, 8)
    .map((item) => ({
      name: (item.testType || "TEST").slice(0, 6),
      accuracy: item.totalQuestions ? Math.round((item.score * 1000) / item.totalQuestions) / 10 : 0,
    }))
    .reverse();
  const avgAccuracy = trendData.length ? Math.round((trendData.reduce((sum, item) => sum + item.accuracy, 0) / trendData.length) * 10) / 10 : 0;

  return (
    <div className="space-y-5">
      <section className="portal-banner">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-100">Staff Workspace</p>
        <h2 className="mt-2 text-3xl font-bold text-white">Student Monitoring Desk</h2>
        <p className="mt-2 text-sm text-cyan-50/90">Track readiness trajectory, identify weak patterns, and drive intervention recommendations.</p>
      </section>

      <div className="dashboard-card p-4">
        <div className="flex flex-wrap items-center gap-2">
          <input className="w-40 rounded-xl border border-slate-200 px-3 py-2 text-sm" type="number" min="1" value={studentId} onChange={(e) => setStudentId(e.target.value)} />
          <button type="button" className="rounded-xl bg-slate-900 px-3 py-2 text-sm font-semibold text-white" onClick={inspect}>Inspect Student</button>
          {loading ? <span className="text-xs text-slate-500">Loading...</span> : null}
        </div>
      </div>

      {error ? <p className="text-sm text-rose-600">{error}</p> : null}

      {details ? (
        <div className="grid gap-4 lg:grid-cols-2">
          <div className="dashboard-card p-5">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-slate-900">{details.name}</h3>
                <p className="text-sm text-slate-600">{details.department}</p>
              </div>
              <RiskBadge readiness={details.readiness} />
            </div>
            <div className="mt-3 space-y-2">
              {[["Readiness", details.readiness||0, "from-teal-500 to-emerald-400"],
                ["Aptitude", details.aptitude||0, "from-sky-500 to-blue-400"],
                ["Coding", details.coding||0, "from-indigo-500 to-violet-400"],
                ["Soft Skills", details.softSkills||0, "from-rose-400 to-pink-400"],
              ].map(([label, val, color]) => (
                <div key={label}>
                  <div className="mb-1 flex justify-between text-sm">
                    <span className="font-medium text-slate-600">{label}</span>
                    <span className="font-bold text-slate-800">{val}%</span>
                  </div>
                  <div className="h-2 w-full rounded-full bg-slate-100 overflow-hidden">
                    <div className={`h-2 rounded-full bg-gradient-to-r ${color} transition-all duration-700`} style={{width: `${val}%`}} />
                  </div>
                </div>
              ))}
            </div>
            <p className="mt-3 text-sm text-slate-700">Average recent accuracy: <span className="font-semibold">{avgAccuracy}%</span></p>
            {Array.isArray(details.weakAreas) && details.weakAreas.length > 0 && (
              <div className="mt-3 rounded-xl bg-amber-50 border border-amber-100 p-3">
                <p className="text-xs font-bold text-amber-700 mb-2">⚠️ Weak Areas</p>
                <div className="flex flex-wrap gap-1.5">
                  {details.weakAreas.map(w => (
                    <span key={w} className="rounded-full bg-amber-100 text-amber-800 px-2 py-0.5 text-[10px] font-semibold">{w}</span>
                  ))}
                </div>
              </div>
            )}
          </div>

          <div className="dashboard-card h-80 p-4">
            <p className="mb-3 font-semibold text-slate-900">Recent Accuracy Trend</p>
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={trendData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis domain={[0, 100]} />
                <Tooltip />
                <Line type="monotone" dataKey="accuracy" stroke="#0f766e" strokeWidth={3} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      ) : null}
    </div>
  );
};
export const InterviewerWorkbenchPage = () => {
  const [queue, setQueue] = useState([]);
  const [activeId, setActiveId] = useState(null);
  const [scorecards, setScorecards] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const loadQueue = async () => {
    setLoading(true);
    setError("");
    try {
      const { data } = await api.get("/api/portal/staff/interviews/queue");
      const list = Array.isArray(data) ? data : [];
      setQueue(list);
      if (list.length > 0 && !activeId) {
        setActiveId(list[0].interviewScheduleId);
      }
      setScorecards((prev) => {
        const next = { ...prev };
        list.forEach((item) => {
          if (!next[item.interviewScheduleId]) {
            next[item.interviewScheduleId] = {
              technicalScore: 8,
              communicationScore: 8,
              confidenceScore: 8,
              recommendation: "Hold",
              comments: "",
            };
          }
        });
        return next;
      });
    } catch (e) {
      setError(e.response?.data?.message || "Failed to load interviewer queue.");
      setQueue([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadQueue();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const active = queue.find((item) => item.interviewScheduleId === activeId);
  const activeCard = active ? scorecards[active.interviewScheduleId] || {} : null;

  const submitFeedback = async () => {
    if (!active || !activeCard) return;
    setError("");
    setMessage("");
    try {
      await api.post(`/api/portal/staff/interviews/${active.interviewScheduleId}/feedback`, activeCard);
      setMessage("Feedback submitted successfully.");
      await loadQueue();
    } catch (e) {
      setError(e.response?.data?.message || "Failed to submit feedback.");
    }
  };

  if (loading) return <p>Loading interviewer workbench...</p>;

  return (
    <div className="space-y-5">
      <section className="portal-banner">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-100">Interviewer Workspace</p>
        <h2 className="mt-2 text-3xl font-bold text-white">Interview Workbench</h2>
        <p className="mt-2 text-sm text-cyan-50/90">Review assigned interviews and submit structured scorecards.</p>
      </section>

      {error ? <p className="text-sm text-rose-600">{error}</p> : null}
      {message ? <p className="text-sm text-emerald-700">{message}</p> : null}

      <div className="grid gap-4 lg:grid-cols-[320px_1fr]">
        <div className="dashboard-card p-4">
          <h3 className="font-semibold text-slate-900">Assigned Queue</h3>
          <ul className="mt-3 space-y-2 text-sm">
            {queue.length === 0 ? <li className="text-slate-600">No pending interviews.</li> : null}
            {queue.map((item) => (
              <li key={item.interviewScheduleId}>
                <button
                  type="button"
                  className={`w-full rounded-xl border px-3 py-2 text-left ${activeId === item.interviewScheduleId ? "border-cyan-400 bg-cyan-50 text-cyan-900" : "border-slate-200 bg-white text-slate-700"}`}
                  onClick={() => setActiveId(item.interviewScheduleId)}
                >
                  <p className="font-semibold">{item.studentName}</p>
                  <p>{item.jobTitle}</p>
                  <p className="text-xs">{formatDateTime(item.scheduledAt)}</p>
                </button>
              </li>
            ))}
          </ul>
        </div>

        <div className="dashboard-card p-5">
          {!active ? <p className="text-sm text-slate-600">Select a queue item to submit feedback.</p> : (
            <>
              <h3 className="text-lg font-semibold text-slate-900">Feedback Scorecard</h3>
              <p className="mt-1 text-sm text-slate-600">{active.studentName} | {active.jobTitle} | {active.companyName}</p>
              <div className="mt-4 grid gap-3 md:grid-cols-2">
                <label className="text-sm text-slate-700">Technical (1-10)
                  <input className="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2" type="number" min="1" max="10" value={activeCard.technicalScore || 8} onChange={(e) => setScorecards((prev) => ({ ...prev, [active.interviewScheduleId]: { ...activeCard, technicalScore: Number(e.target.value) } }))} />
                </label>
                <label className="text-sm text-slate-700">Communication (1-10)
                  <input className="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2" type="number" min="1" max="10" value={activeCard.communicationScore || 8} onChange={(e) => setScorecards((prev) => ({ ...prev, [active.interviewScheduleId]: { ...activeCard, communicationScore: Number(e.target.value) } }))} />
                </label>
                <label className="text-sm text-slate-700">Confidence (1-10)
                  <input className="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2" type="number" min="1" max="10" value={activeCard.confidenceScore || 8} onChange={(e) => setScorecards((prev) => ({ ...prev, [active.interviewScheduleId]: { ...activeCard, confidenceScore: Number(e.target.value) } }))} />
                </label>
                <label className="text-sm text-slate-700">Recommendation
                  <select className="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2" value={activeCard.recommendation || "Hold"} onChange={(e) => setScorecards((prev) => ({ ...prev, [active.interviewScheduleId]: { ...activeCard, recommendation: e.target.value } }))}>
                    {RECOMMENDATIONS.map((option) => <option key={option}>{option}</option>)}
                  </select>
                </label>
              </div>
              <textarea className="mt-3 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm" rows={4} placeholder="Feedback comments" value={activeCard.comments || ""} onChange={(e) => setScorecards((prev) => ({ ...prev, [active.interviewScheduleId]: { ...activeCard, comments: e.target.value } }))} />
              <button type="button" className="mt-3 rounded-xl bg-teal-700 px-4 py-2 text-sm font-semibold text-white" onClick={submitFeedback}>
                Submit Feedback
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  );
};
