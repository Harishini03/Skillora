import { startTransition, useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import {
  LineChart, Line, CartesianGrid, XAxis, YAxis, Tooltip, ResponsiveContainer, BarChart, Bar,
} from "recharts";
import api from "../lib/api";
import { ProgressBar, StatCard } from "../components/AppShell";
import { downloadCsv } from "../lib/csv";
import { isFirebaseConfigured, saveFirebaseRecord } from "../lib/firebaseStore";

const usePersistentState = (key, initialValue) => {
  const [value, setValue] = useState(() => {
    const raw = localStorage.getItem(key);
    if (!raw) return initialValue;
    try {
      return JSON.parse(raw);
    } catch {
      return initialValue;
    }
  });

  useEffect(() => {
    localStorage.setItem(key, JSON.stringify(value));
  }, [key, value]);

  return [value, setValue];
};

const readSessionSnapshot = () => {
  try {
    const raw = localStorage.getItem("pi_session");
    return raw ? JSON.parse(raw) : {};
  } catch {
    return {};
  }
};

export const StudentDashboardPage = () => {
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    api.get("/api/student/dashboard")
      .then(res => { if (active) setData(res.data); })
      .catch(e => { if (active) setError(e.response?.data?.message || "Could not load dashboard. Ensure backend is running on :8080."); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);

  if (loading) return (
    <div className="space-y-5">
      <div className="portal-banner animate-pulse">
        <div className="h-4 w-48 rounded bg-white/20 mb-2" />
        <div className="h-8 w-72 rounded bg-white/20" />
      </div>
      <div className="grid gap-4 md:grid-cols-4">
        {[...Array(4)].map((_, i) => (
          <div key={i} className="dashboard-card p-5 animate-pulse">
            <div className="h-3 w-20 rounded bg-slate-200 mb-3" />
            <div className="h-8 w-16 rounded bg-slate-200" />
          </div>
        ))}
      </div>
    </div>
  );

  if (error) return (
    <div className="dashboard-card p-10 text-center">
      <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-rose-100">
        <svg className="h-7 w-7 text-rose-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" />
        </svg>
      </div>
      <p className="text-base font-semibold text-slate-800">Failed to load dashboard</p>
      <p className="mt-1 text-sm text-rose-600">{error}</p>
      <button onClick={() => window.location.reload()} className="mt-4 rounded-xl bg-teal-700 px-5 py-2 text-sm font-semibold text-white hover:bg-teal-600">
        Retry
      </button>
    </div>
  );

  const readiness = data?.readinessScore || 0;
  const aptitude  = data?.aptitudeProgress || 0;
  const coding    = data?.codingProgress || 0;
  const mock      = data?.softSkillsProgress || 0;
  const weakAreas = Array.isArray(data?.weakAreas) ? data.weakAreas : [];
  const recs      = Array.isArray(data?.recommendations) ? data.recommendations : [];

  const ringColor = readiness >= 70 ? "#10b981" : readiness >= 40 ? "#f59e0b" : "#ef4444";
  const ringLabel = readiness >= 70 ? "🌟 Placement Ready" : readiness >= 40 ? "📈 On Track" : "⚡ Needs Work";
  const ringCircumference = 2 * Math.PI * 52;
  const ringDash = (readiness / 100) * ringCircumference;

  const dailyTips = [
    "Practice 2 DSA problems daily — consistency beats cramming.",
    "Review formulas 10 min before sleeping for better retention.",
    "Attempt a Mock Test every Sunday to simulate placement pressure.",
    "For aptitude, focus on shortcuts — not derivations.",
    "Read one company's interview experience today on GeeksforGeeks.",
  ];
  const todayTip = dailyTips[new Date().getDay() % dailyTips.length];

  const statCards = [
    { icon: "🎯", label: "Readiness", value: `${readiness}%`, bg: "bg-teal-50", text: "text-teal-700", border: "border-teal-200" },
    { icon: "🎓", label: "CGPA", value: data?.cgpa ?? "—", bg: "bg-sky-50", text: "text-sky-700", border: "border-sky-200" },
    { icon: "🏛️", label: "Department", value: data?.department || "N/A", bg: "bg-purple-50", text: "text-purple-700", border: "border-purple-200", small: true },
    { icon: "⚠️", label: "Weak Areas", value: weakAreas.length, bg: "bg-amber-50", text: "text-amber-700", border: "border-amber-200" },
  ];

  const progressBars = [
    { label: "Aptitude", value: aptitude, color: "from-teal-500 to-emerald-400", href: "/student/aptitude", icon: "📊" },
    { label: "Coding (DSA)", value: coding, color: "from-sky-500 to-blue-400", href: "/student/dsa", icon: "💻" },
    { label: "Mock Tests", value: mock, color: "from-purple-500 to-violet-400", href: "/student/mock", icon: "🏆" },
  ];

  const quickActions = [
    { icon: "📝", title: "Aptitude Test", desc: "20 AI-generated placement MCQs", href: "/student/aptitude", grad: "from-teal-600 to-emerald-500", badge: "20 Qs" },
    { icon: "💻", title: "DSA Practice", desc: "LeetCode-style coding problems", href: "/student/dsa", grad: "from-sky-600 to-blue-500", badge: "5 Qs" },
    { icon: "🤖", title: "AI Mentor", desc: "Learn, practice, revise with AI", href: "/student/ai-mentor", grad: "from-purple-600 to-violet-500", badge: "Groq AI" },
    { icon: "🏆", title: "Mock Test", desc: "Full 20+2 campus simulation", href: "/student/mock", grad: "from-rose-600 to-pink-500", badge: "90 min" },
    { icon: "📚", title: "Jobs Board", desc: "Explore campus opportunities", href: "/student/jobs", grad: "from-indigo-600 to-blue-600", badge: "New" },
    { icon: "📊", title: "My Reports", desc: "View performance analytics", href: "/student/reports", grad: "from-amber-500 to-orange-500", badge: "Charts" },
  ];

  return (
    <div className="space-y-6">
      {/* Premium Banner */}
      <section className="portal-banner relative overflow-hidden">
        <div className="absolute inset-0 opacity-10" style={{backgroundImage: "radial-gradient(circle at 20% 50%, #06b6d4 0%, transparent 50%), radial-gradient(circle at 80% 20%, #8b5cf6 0%, transparent 40%)"}} />
        <div className="relative">
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-100">Student Dashboard</p>
          <h2 className="mt-1 text-3xl font-bold text-white">Welcome back! 👋</h2>
          <p className="mt-1 text-sm text-cyan-50/80">Your placement journey continues — track readiness, sharpen skills, and land your dream job.</p>
          <div className="mt-4 inline-flex items-center gap-2 rounded-xl bg-white/10 backdrop-blur-sm border border-white/20 px-4 py-2">
            <span className="text-lg">💡</span>
            <p className="text-xs text-white/90 font-medium">{todayTip}</p>
          </div>
        </div>
      </section>

      {/* Readiness ring + stat cards */}
      <div className="grid gap-4 md:grid-cols-[auto_1fr_1fr_1fr]">
        {/* Ring */}
        <div className="dashboard-card flex flex-col items-center justify-center p-6">
          <svg width="130" height="130" viewBox="0 0 130 130" aria-label={`Readiness: ${readiness}%`}>
            <circle cx="65" cy="65" r="52" fill="none" stroke="#e2e8f0" strokeWidth="11" />
            <circle cx="65" cy="65" r="52" fill="none" stroke={ringColor} strokeWidth="11"
              strokeDasharray={`${ringDash} ${ringCircumference}`}
              strokeLinecap="round" transform="rotate(-90 65 65)"
              style={{ transition: "stroke-dasharray 1s ease" }} />
            <text x="65" y="61" textAnchor="middle" fontSize="22" fontWeight="800" fill={ringColor}>{readiness}</text>
            <text x="65" y="76" textAnchor="middle" fontSize="10" fill="#64748b">READINESS %</text>
          </svg>
          <p className="mt-2 text-xs font-semibold text-slate-500 uppercase tracking-wide">Overall Score</p>
          <span className="mt-1 rounded-full px-2.5 py-0.5 text-[10px] font-bold" style={{background: ringColor + "20", color: ringColor}}>{ringLabel}</span>
        </div>

        {/* Stat cards */}
        {statCards.map(s => (
          <div key={s.label} className={`dashboard-card flex flex-col justify-center border ${s.border} ${s.bg} p-5 hover:shadow-md transition`}>
            <p className="text-2xl">{s.icon}</p>
            <p className={`mt-2 ${s.small ? "text-lg" : "text-2xl"} font-bold ${s.text}`}>{s.value}</p>
            <p className="mt-0.5 text-xs font-medium text-slate-500 uppercase tracking-wide">{s.label}</p>
          </div>
        ))}
      </div>

      {/* Progress bars */}
      <div className="dashboard-card p-5">
        <h3 className="mb-4 text-base font-bold text-slate-900">Section Progress</h3>
        <div className="space-y-4">
          {progressBars.map(pb => (
            <button key={pb.label} onClick={() => navigate(pb.href)} className="w-full text-left group">
              <div className="mb-1.5 flex items-center justify-between text-sm">
                <span className="flex items-center gap-2 font-semibold text-slate-700 group-hover:text-teal-700 transition">
                  <span>{pb.icon}</span>{pb.label}
                </span>
                <span className="font-bold text-slate-900">{Math.round(pb.value)}%</span>
              </div>
              <div className="h-2.5 w-full overflow-hidden rounded-full bg-slate-100">
                <div className={`h-2.5 rounded-full bg-gradient-to-r ${pb.color} transition-all duration-700`}
                  style={{ width: `${pb.value}%` }} />
              </div>
            </button>
          ))}
        </div>
      </div>

      {/* Weak areas + recommendations */}
      <div className="grid gap-4 lg:grid-cols-2">
        <div className="dashboard-card p-5">
          <h3 className="mb-3 flex items-center gap-2 text-base font-bold text-slate-900">
            <span className="flex h-6 w-6 items-center justify-center rounded-full bg-amber-100 text-amber-600 text-xs">⚠</span>
            Weak Areas
          </h3>
          {weakAreas.length === 0
            ? <p className="text-sm text-slate-500">No weak areas identified yet. Take a test to find out!</p>
            : <ul className="space-y-2">
                {weakAreas.map((area, i) => (
                  <li key={i} className="flex items-center justify-between rounded-xl bg-rose-50 border border-rose-100 px-3 py-2">
                    <span className="flex items-center gap-2 text-sm text-rose-800 font-medium">
                      <span className="h-2 w-2 rounded-full bg-rose-400 flex-shrink-0" />{area}
                    </span>
                    <button onClick={() => navigate(`/student/ai-mentor`)} className="text-[10px] text-rose-600 underline hover:text-rose-800">Revise →</button>
                  </li>
                ))}
              </ul>
          }
        </div>
        <div className="dashboard-card p-5">
          <h3 className="mb-3 flex items-center gap-2 text-base font-bold text-slate-900">
            <span className="flex h-6 w-6 items-center justify-center rounded-full bg-teal-100 text-teal-600 text-xs">→</span>
            AI Recommendations
          </h3>
          {recs.length === 0
            ? <p className="text-sm text-slate-500">Complete a test to get personalized recommendations.</p>
            : <ul className="space-y-2">
                {recs.map((rec, i) => (
                  <li key={i} className="flex items-start gap-2 rounded-xl bg-teal-50 border border-teal-100 px-3 py-2 text-sm text-teal-800">
                    <span className="mt-0.5 text-teal-500 font-bold">→</span>
                    {rec}
                  </li>
                ))}
              </ul>
          }
        </div>
      </div>

      {/* Quick Actions */}
      <div>
        <h3 className="mb-3 text-base font-bold text-slate-900">Quick Actions</h3>
        <div className="grid gap-4 md:grid-cols-3">
          {quickActions.map(qa => (
            <button key={qa.title} onClick={() => navigate(qa.href)}
              className={`group relative overflow-hidden rounded-2xl bg-gradient-to-br ${qa.grad} p-5 text-left shadow-lg transition hover:scale-[1.02] hover:shadow-xl`}>
              <div className="flex items-start justify-between">
                <div className="text-3xl mb-3">{qa.icon}</div>
                <span className="rounded-full bg-white/20 px-2 py-0.5 text-[10px] font-bold text-white/90">{qa.badge}</span>
              </div>
              <p className="font-bold text-white text-base">{qa.title}</p>
              <p className="mt-1 text-xs text-white/80">{qa.desc}</p>
              <span className="absolute right-4 top-1/2 -translate-y-1/2 text-white/40 text-2xl group-hover:text-white/80 transition">→</span>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};

const SECTION_TOPICS = {
  aptitude: ["Percentages", "Profit & Loss", "Time & Work", "Speed, Distance & Time", "HCF & LCM", "Probability"],
  coding: ["Arrays", "Binary Trees", "Dynamic Programming", "Graphs", "Stacks & Queues", "Sorting"],
};

const FORMULAS = {
  "Percentages": [
    { title: "Net % Change", formula: "A + B + (AB / 100)" },
    { title: "Increase %", formula: "(Change / Original) * 100" }
  ],
  "Profit & Loss": [
    { title: "Profit %", formula: "(Profit / CP) * 100" },
    { title: "SP with Discount", formula: "MP * (100 - Discount%) / 100" }
  ],
  "Time & Work": [
    { title: "Combined (2 people)", formula: "(A * B) / (A + B) days" },
    { title: "Work done", formula: "Efficiency * Time" }
  ],
  "Speed, Distance & Time": [
    { title: "Relative speed (Opposite)", formula: "S1 + S2" },
    { title: "Conversion km/h to m/s", formula: "Speed * 5/18" }
  ],
  "HCF & LCM": [
    { title: "Product rule", formula: "Num1 * Num2 = HCF * LCM" },
    { title: "HCF of Fractions", formula: "HCF(Numerators) / LCM(Denominators)" }
  ],
  "Probability": [
    { title: "Basic P(E)", formula: "Favorable / Total" },
    { title: "Complement rule", formula: "P(E') = 1 - P(E)" }
  ]
};

export const StudentSectionPage = ({ section }) => {
  const location = useLocation();
  const navigate = useNavigate();
  const selectedTopic = useMemo(() => {
    try {
      return (new URLSearchParams(location.search).get("topic") || "").trim();
    } catch {
      return "";
    }
  }, [location.search]);

  const selectTopic = (t) => {
    navigate(`?topic=${encodeURIComponent(t)}`);
  };

  const clearTopic = () => {
    navigate("?");
  };

  // Phase: "start" | "active" | "results"
  const [phase, setPhase] = useState("start");
  const [activeTab, setActiveTab] = useState("test"); // "test" | "history"
  const [test, setTest] = useState(null);
  const [answers, setAnswers] = useState({});
  const [currentIndex, setCurrentIndex] = useState(0);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [language, setLanguage] = useState("python");
  const [runResult, setRunResult] = useState(null);
  const [secondsLeft, setSecondsLeft] = useState(0);
  const [history, setHistory] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(false);
  const starter = {
    python: `n = int(input().strip())
print(n * 2)
`,
    java: `import java.util.*;
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        System.out.println(n * 2);
    }
}
`,
    cpp: `#include <bits/stdc++.h>
using namespace std;
int main() {
    int n;
    cin >> n;
    cout << n * 2 << "\\n";
    return 0;
}
`,
  };
  const [code, setCode] = useState(starter.python);
  const sampleCases = [
    { input: "2", expected: "4" },
    { input: "7", expected: "14" },
  ];
  const answeredCount = useMemo(() => Object.keys(answers).length, [answers]);
  const progressPercent = useMemo(() => {
    if (!test?.totalQuestions) return 0;
    return Math.round((answeredCount / test.totalQuestions) * 100);
  }, [answeredCount, test]);

  const storageKey = useMemo(() => {
    if (!test?.sessionId) return "";
    return `skillora_answers_${section}_${test.sessionId}`;
  }, [section, test]);

  const sectionLabel = {
    aptitude: "Aptitude",
    coding: "Coding",
    softskills: "Soft Skills",
    soft_skills: "Soft Skills",
    mock: "Mock Test",
  }[section?.toLowerCase()] || section?.replace("-", " ");

  const loadHistory = async () => {
    setHistoryLoading(true);
    try {
      const res = await api.get("/api/student/test-history");
      setHistory(Array.isArray(res.data) ? res.data : []);
    } catch {
      setHistory([]);
    } finally {
      setHistoryLoading(false);
    }
  };

  const loadTest = () => {
    setResult(null);
    setError("");
    setLoading(true);
    setPhase("start");
    setAnswers({});
    setCurrentIndex(0);
    const params = { section };
    if (selectedTopic) {
      params.topic = selectedTopic;
    }
    const normalizeTestPayload = (incoming) => ({
      ...incoming,
      questions: Array.isArray(incoming?.questions) ? incoming.questions : [],
      totalQuestions: Number.isFinite(incoming?.totalQuestions) ? incoming.totalQuestions : 0,
      durationMinutes: Number.isFinite(incoming?.durationMinutes) ? incoming.durationMinutes : 0,
    });

    const applyTest = (incoming) => {
      const normalized = normalizeTestPayload(incoming || {});
      setTest(normalized);
      setSecondsLeft((normalized.durationMinutes || 0) * 60);
      return normalized;
    };

    api.get("/api/student/tests", { params })
      .then((res) => {
        applyTest(res?.data);
      })
      .catch(async (e) => {
        if (e?.response?.status === 401 || e?.response?.status === 403) {
          setError("Session expired or unauthorized. Please login again.");
          return;
        }
        setError(e?.response?.data?.message || `Failed to load test (status ${e?.response?.status || "network"}).`);
      })
      .finally(() => {
        setLoading(false);
      });
  };

  useEffect(() => {
    loadTest();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [section, selectedTopic]);

  const startTest = () => {
    setPhase("active");
    setCurrentIndex(0);
    setAnswers({});
    setSecondsLeft((test?.durationMinutes || 0) * 60);
  };

  const submit = async (isAutoSubmit = false) => {
    if (!test || submitting) return;
    setSubmitting(true);
    setError("");
    const timeTaken = Math.max(0, (test.durationMinutes * 60) - secondsLeft);
    const payload = {
      testType: test.testType,
      sessionId: test.sessionId,
      timeTakenSeconds: timeTaken,
      answers,
    };
    try {
      const { data } = await api.post("/api/student/submit-test", payload);
      startTransition(() => {
        setResult(data);
        setPhase("results");
      });
      localStorage.removeItem(storageKey);
    } catch (e) {
      if (!isAutoSubmit) {
        setError(e.response?.data?.message || "Submission failed. Try again.");
      }
    } finally {
      setSubmitting(false);
    }
  };

  const runCode = async () => {
    if (section !== "coding") return;
    setError("");
    const executions = await Promise.all(
      sampleCases.map(async (testCase) => {
        const { data } = await api.post("/api/student/code/execute", {
          language,
          code,
          stdin: testCase.input,
        });
        const actual = (data.stdout || "").trim();
        return {
          ...testCase,
          actual,
          stderr: data.stderr,
          success: data.success && actual === testCase.expected,
        };
      }),
    );
    const passed = executions.filter((e) => e.success).length;
    startTransition(() => {
      setRunResult({ passed, total: executions.length, cases: executions });
    });
  };

  useEffect(() => {
    if (!storageKey) return;
    localStorage.setItem(storageKey, JSON.stringify(answers));
  }, [answers, storageKey]);

  useEffect(() => {
    if (phase !== "active" || !test || secondsLeft <= 0 || result || submitting) return;
    const timer = setInterval(() => {
      setSecondsLeft((prev) => Math.max(0, prev - 1));
    }, 1000);
    return () => clearInterval(timer);
  }, [phase, test, secondsLeft, result, submitting]);

  useEffect(() => {
    if (secondsLeft === 0 && phase === "active" && test && !result && !submitting) {
      submit(true);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [secondsLeft]);

  // Derived
  const questionList = Array.isArray(test?.questions) ? test.questions : [];
  const currentQuestion = questionList[currentIndex] || null;
  const timerMinutes = Math.floor(secondsLeft / 60);
  const timerSeconds = secondsLeft % 60;
  const timerCritical = secondsLeft <= 60 && secondsLeft > 0;
  const timerWarning = secondsLeft <= 300 && secondsLeft > 60;

  if (loading) return (
    <div className="flex items-center justify-center min-h-[400px]">
      <div className="text-center">
        <div className="inline-block h-12 w-12 animate-spin rounded-full border-4 border-solid border-teal-600 border-r-transparent"></div>
        <p className="mt-4 text-slate-600">Preparing your test...</p>
      </div>
    </div>
  );

  if (error && !test) return (
    <div className="dashboard-card p-8 text-center">
      <p className="text-lg font-semibold text-rose-600">{error}</p>
      <button type="button" onClick={loadTest} className="mt-4 rounded-xl bg-teal-700 px-4 py-2 text-sm font-semibold text-white hover:bg-teal-600">Retry</button>
    </div>
  );

  if (!test) return <p className="text-slate-600">No active test available.</p>;

  return (
    <div className="space-y-5">
      {/* Banner */}
      <div className="portal-banner flex items-center justify-between">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-100">Assessment Module</p>
          <h2 className="mt-1 text-3xl font-bold capitalize text-white">{sectionLabel} Test</h2>
          {selectedTopic ? <p className="mt-1 text-xs text-cyan-100">Topic: {selectedTopic}</p> : null}
        </div>
        <div className="flex gap-2">
          <button
            type="button"
            onClick={() => { setActiveTab("test"); if (phase === "results") loadTest(); }}
            className={`rounded-xl px-3 py-2 text-sm font-semibold transition ${activeTab === "test" ? "bg-white text-teal-800" : "border border-white/40 text-white hover:bg-white/10"}`}
          >
            Take Test
          </button>
          <button
            type="button"
            onClick={() => { setActiveTab("history"); loadHistory(); }}
            className={`rounded-xl px-3 py-2 text-sm font-semibold transition ${activeTab === "history" ? "bg-white text-teal-800" : "border border-white/40 text-white hover:bg-white/10"}`}
          >
            History
          </button>
        </div>
      </div>

      {/* History Tab */}
      {activeTab === "history" && (
        <div className="dashboard-card p-5">
          <h3 className="text-lg font-semibold text-slate-900">Test History</h3>
          {historyLoading ? (
            <p className="mt-3 text-sm text-slate-500">Loading history...</p>
          ) : history.length === 0 ? (
            <p className="mt-3 text-sm text-slate-500">No past attempts found. Take your first test!</p>
          ) : (
            <div className="mt-3 overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-slate-200 text-left text-xs font-semibold uppercase text-slate-500">
                    <th className="pb-2 pr-4">Date</th>
                    <th className="pb-2 pr-4">Type</th>
                    <th className="pb-2 pr-4">Score</th>
                    <th className="pb-2">Accuracy</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {history.map((item) => {
                    const acc = item.totalQuestions ? Math.round((item.score / item.totalQuestions) * 100) : 0;
                    const dateStr = item.testDate ? new Date(item.testDate).toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" }) : "—";
                    return (
                      <tr key={item.attemptId} className="hover:bg-slate-50">
                        <td className="py-2 pr-4 text-slate-600">{dateStr}</td>
                        <td className="py-2 pr-4 font-medium text-slate-900">{item.testType}</td>
                        <td className="py-2 pr-4 text-slate-700">{item.score}/{item.totalQuestions}</td>
                        <td className="py-2">
                          <span className={`rounded-full px-2 py-0.5 text-xs font-semibold ${acc >= 70 ? "bg-emerald-100 text-emerald-800" : acc >= 40 ? "bg-amber-100 text-amber-800" : "bg-rose-100 text-rose-800"}`}>
                            {acc}%
                          </span>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Test Tab */}
      {activeTab === "test" && (
        <>
          {/* START SCREEN */}
          {phase === "start" && (
            <div className="grid gap-6 md:grid-cols-[1fr_300px]">
              {/* Left Column: Test Setup & Info */}
              <div className="dashboard-card p-6 flex flex-col justify-between">
                <div>
                  <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-teal-100">
                    <svg className="h-7 w-7 text-teal-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                    </svg>
                  </div>
                  <h3 className="text-xl font-bold text-slate-900 text-center">{sectionLabel} Assessment</h3>
                  
                  {/* Topic selection if available */}
                  {SECTION_TOPICS[section?.toLowerCase()] && (
                    <div className="mt-4 border-t border-b border-slate-100 py-3 text-left">
                      <p className="text-xs font-semibold uppercase text-slate-400 mb-2">Practice by Topic:</p>
                      <div className="flex flex-wrap gap-1.5">
                        <button
                          type="button"
                          onClick={() => clearTopic()}
                          className={`rounded-full px-3 py-1 text-xs font-medium transition ${!selectedTopic ? "bg-teal-700 text-white" : "bg-slate-100 text-slate-600 hover:bg-slate-200"}`}
                        >
                          All Topics
                        </button>
                        {SECTION_TOPICS[section.toLowerCase()].map(t => (
                          <button
                            key={t}
                            type="button"
                            onClick={() => selectTopic(t)}
                            className={`rounded-full px-3 py-1 text-xs font-medium transition ${selectedTopic === t ? "bg-teal-700 text-white" : "bg-slate-100 text-slate-600 hover:bg-slate-200"}`}
                          >
                            {t}
                          </button>
                        ))}
                      </div>
                    </div>
                  )}

                  {selectedTopic && <p className="mt-3 text-sm font-semibold text-teal-800 text-center">Active Topic: {selectedTopic}</p>}
                  
                  <div className="mt-6 grid grid-cols-3 gap-4 text-center">
                    <div className="rounded-xl bg-slate-50 p-3">
                      <p className="text-2xl font-bold text-teal-700">{test.totalQuestions}</p>
                      <p className="text-[10px] uppercase font-semibold text-slate-500">Questions</p>
                    </div>
                    <div className="rounded-xl bg-slate-50 p-3">
                      <p className="text-2xl font-bold text-teal-700">{test.durationMinutes}</p>
                      <p className="text-[10px] uppercase font-semibold text-slate-500">Minutes</p>
                    </div>
                    <div className="rounded-xl bg-slate-50 p-3">
                      <p className="text-2xl font-bold text-teal-700">MCQ</p>
                      <p className="text-[10px] uppercase font-semibold text-slate-500">Format</p>
                    </div>
                  </div>

                  <ul className="mt-6 space-y-1.5 text-left text-xs text-slate-600 pl-4 list-disc">
                    <li>Each question is timed and counts towards your profile readiness score.</li>
                    <li>The assessment auto-submits when the timer reaches 0.</li>
                    <li>Ensure a stable network connection before starting.</li>
                  </ul>
                </div>

                <div className="mt-6 flex justify-center gap-3 border-t border-slate-50 pt-4">
                  <button type="button" onClick={loadTest} className="rounded-xl border border-slate-300 px-4 py-2 text-sm hover:bg-slate-50 font-medium">
                    Regenerate Test
                  </button>
                  <button
                    type="button"
                    onClick={startTest}
                    disabled={questionList.length === 0}
                    className="rounded-xl bg-teal-700 px-6 py-2 font-semibold text-white hover:bg-teal-600 disabled:cursor-not-allowed disabled:opacity-60 shadow-md"
                  >
                    {questionList.length === 0 ? "No Questions Available" : "Start Test"}
                  </button>
                </div>
                {error && <p className="mt-3 text-sm text-rose-600 text-center">{error}</p>}
              </div>

              {/* Right Column: Revision sheet & Formulas */}
              <div className="dashboard-card p-5 bg-slate-50 border border-slate-100 flex flex-col">
                <div className="flex items-center gap-2 mb-3">
                  <span className="text-lg">⚡</span>
                  <h4 className="font-bold text-slate-900 text-sm">Quick Formulas & Revise</h4>
                </div>
                {section === "aptitude" && selectedTopic && FORMULAS[selectedTopic] ? (
                  <div className="space-y-3 flex-1 overflow-y-auto max-h-[300px] pr-1">
                    <p className="text-xs text-slate-500">Essential rules for <strong>{selectedTopic}</strong>:</p>
                    {FORMULAS[selectedTopic].map((f, idx) => (
                      <div key={idx} className="rounded-lg bg-white border border-slate-100 p-2.5 shadow-sm">
                        <p className="text-xs font-bold text-slate-700">{f.title}</p>
                        <p className="mt-1 font-mono text-[11px] text-teal-800 bg-teal-50/50 p-1.5 rounded">{f.formula}</p>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="flex-1 flex flex-col items-center justify-center text-center py-8 text-slate-500">
                    <span className="text-2xl mb-1">📖</span>
                    <p className="text-xs font-semibold">Select an aptitude topic to view formula flashcards.</p>
                    <p className="text-[10px] text-slate-400 mt-1">Revise key shortcuts right before your placement test starts.</p>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* ACTIVE TEST */}
          {phase === "active" && currentQuestion && (
            <div className="space-y-4">
              {/* Timer + progress bar */}
              <div className="dashboard-card flex items-center justify-between p-4">
                <div className="flex items-center gap-3">
                  <span className="text-sm font-semibold text-slate-700">
                    Q{currentIndex + 1}/{questionList.length}
                  </span>
                  <span className="text-xs text-slate-500">{answeredCount} answered</span>
                </div>
                <div className={`rounded-xl px-4 py-1.5 text-sm font-bold tabular-nums ${timerCritical ? "bg-rose-100 text-rose-700 animate-pulse" : timerWarning ? "bg-amber-100 text-amber-700" : "bg-teal-100 text-teal-800"}`}>
                  ⏱ {String(timerMinutes).padStart(2, "0")}:{String(timerSeconds).padStart(2, "0")}
                </div>
              </div>

              {/* Question status dots */}
              <div className="flex flex-wrap gap-1.5">
                {questionList.map((q, i) => (
                  <button
                    key={q.id}
                    type="button"
                    onClick={() => setCurrentIndex(i)}
                    className={`h-7 w-7 rounded-lg text-xs font-semibold transition ${
                      i === currentIndex
                        ? "bg-sky-600 text-white"
                        : answers[q.id]
                        ? "bg-emerald-500 text-white"
                        : "bg-slate-200 text-slate-600 hover:bg-slate-300"
                    }`}
                  >
                    {i + 1}
                  </button>
                ))}
              </div>

              {/* Current question */}
              <div className="dashboard-card p-5">
                <p className="text-xs font-semibold uppercase text-slate-400">
                  {currentQuestion.topic} · {currentQuestion.difficultyLevel}
                </p>
                <p className="mt-2 text-base font-medium text-slate-900">
                  {currentIndex + 1}. {currentQuestion.questionText}
                </p>
                <div className="mt-4 grid gap-2 md:grid-cols-2">
                  {["A", "B", "C", "D"].map((option) => (
                    <button
                      key={option}
                      type="button"
                      onClick={() => setAnswers((prev) => ({ ...prev, [currentQuestion.id]: option }))}
                      className={`rounded-xl border px-4 py-3 text-left text-sm transition ${
                        answers[currentQuestion.id] === option
                          ? "border-sky-500 bg-sky-50 font-semibold text-sky-900"
                          : "border-slate-200 bg-white hover:border-sky-300 hover:bg-sky-50/50"
                      }`}
                    >
                      <span className={`mr-2 inline-flex h-6 w-6 items-center justify-center rounded-full text-xs font-bold ${answers[currentQuestion.id] === option ? "bg-sky-500 text-white" : "bg-slate-100 text-slate-600"}`}>{option}</span>
                      {currentQuestion[`option${option}`]}
                    </button>
                  ))}
                </div>
              </div>

              {/* Navigation */}
              <div className="flex items-center justify-between">
                <button
                  type="button"
                  onClick={() => setCurrentIndex((i) => Math.max(0, i - 1))}
                  disabled={currentIndex === 0}
                  className="rounded-xl border border-slate-300 px-4 py-2 text-sm font-semibold disabled:cursor-not-allowed disabled:opacity-40 hover:bg-slate-50"
                >
                  ← Previous
                </button>
                {currentIndex < questionList.length - 1 ? (
                  <button
                    type="button"
                    onClick={() => setCurrentIndex((i) => Math.min(questionList.length - 1, i + 1))}
                    className="rounded-xl bg-sky-600 px-4 py-2 text-sm font-semibold text-white hover:bg-sky-500"
                  >
                    Next →
                  </button>
                ) : (
                  <button
                    type="button"
                    disabled={submitting}
                    onClick={() => submit(false)}
                    className="rounded-xl bg-teal-700 px-5 py-2 text-sm font-semibold text-white hover:bg-teal-600 disabled:cursor-not-allowed disabled:opacity-60"
                  >
                    {submitting ? "Submitting..." : "Submit Test"}
                  </button>
                )}
              </div>

              {/* Progress + early submit */}
              <div className="flex items-center justify-between rounded-xl bg-slate-50 px-4 py-3">
                <div className="flex-1">
                  <div className="mb-1 flex justify-between text-xs text-slate-500">
                    <span>Progress</span>
                    <span>{answeredCount}/{test.totalQuestions} ({progressPercent}%)</span>
                  </div>
                  <div className="h-2 w-full overflow-hidden rounded-full bg-slate-200">
                    <div className="h-2 rounded-full bg-teal-500 transition-all" style={{ width: `${progressPercent}%` }} />
                  </div>
                </div>
                {answeredCount > 0 && (
                  <button
                    type="button"
                    disabled={submitting}
                    onClick={() => submit(false)}
                    className="ml-4 rounded-xl border border-teal-600 px-3 py-1.5 text-xs font-semibold text-teal-700 hover:bg-teal-50 disabled:opacity-50"
                  >
                    {submitting ? "Submitting..." : "Submit Now"}
                  </button>
                )}
              </div>
              {error && <p className="text-sm text-rose-600">{error}</p>}
            </div>
          )}

          {/* RESULTS SCREEN */}
          {phase === "results" && result && (
            <div className="space-y-5">
              <div className="dashboard-card p-6">
                <h3 className="text-xl font-bold text-slate-900">Test Complete!</h3>
                <div className="mt-4 grid gap-4 md:grid-cols-4">
                  <div className="rounded-xl bg-teal-50 p-4 text-center">
                    <p className="text-3xl font-bold text-teal-700">{result.score}</p>
                    <p className="text-xs text-slate-500">Correct</p>
                  </div>
                  <div className="rounded-xl bg-rose-50 p-4 text-center">
                    <p className="text-3xl font-bold text-rose-600">{result.totalQuestions - result.score}</p>
                    <p className="text-xs text-slate-500">Incorrect</p>
                  </div>
                  <div className="rounded-xl bg-sky-50 p-4 text-center">
                    <p className="text-3xl font-bold text-sky-700">{result.accuracyPercentage}%</p>
                    <p className="text-xs text-slate-500">Accuracy</p>
                  </div>
                  <div className="rounded-xl bg-slate-100 p-4 text-center">
                    <p className="text-3xl font-bold text-slate-700">{Math.floor(result.timeTakenSeconds / 60)}m {result.timeTakenSeconds % 60}s</p>
                    <p className="text-xs text-slate-500">Time Taken</p>
                  </div>
                </div>
                {Array.isArray(result.weakTopics) && result.weakTopics.length > 0 && (
                  <p className="mt-4 text-sm text-slate-600">
                    <span className="font-semibold">Weak Topics:</span> {result.weakTopics.join(", ")}
                  </p>
                )}
                <button type="button" onClick={loadTest} className="mt-5 rounded-xl bg-teal-700 px-5 py-2 font-semibold text-white hover:bg-teal-600">
                  Take Another Test
                </button>
              </div>

              {/* Per-question review */}
              {Array.isArray(result.reviewItems) && result.reviewItems.length > 0 && (
                <div className="dashboard-card p-5">
                  <h3 className="mb-4 text-lg font-semibold text-slate-900">Question Review</h3>
                  <div className="space-y-4">
                    {result.reviewItems.map((q, idx) => {
                      const chosen = q.selectedOption;
                      const isCorrect = chosen === q.correctOption;
                      return (
                        <div key={q.questionId} className={`rounded-xl border p-4 ${isCorrect ? "border-emerald-200 bg-emerald-50/50" : "border-rose-200 bg-rose-50/50"}`}>
                          <div className="flex items-start gap-2">
                            <span className={`mt-0.5 flex h-5 w-5 shrink-0 items-center justify-center rounded-full text-xs font-bold ${isCorrect ? "bg-emerald-500 text-white" : "bg-rose-500 text-white"}`}>
                              {isCorrect ? "✓" : "✗"}
                            </span>
                            <div>
                              <p className="text-sm font-medium text-slate-900">{idx + 1}. {q.questionText}</p>
                              <p className="mt-0.5 text-xs text-slate-400">{q.topic} · {q.difficultyLevel}</p>
                            </div>
                          </div>
                          <div className="mt-2 grid gap-1.5 pl-7 md:grid-cols-2">
                            {["A", "B", "C", "D"].map((opt) => (
                              <div
                                key={opt}
                                className={`rounded-lg px-3 py-1.5 text-xs ${
                                  opt === q.correctOption
                                    ? "bg-emerald-100 font-semibold text-emerald-800"
                                    : opt === chosen && !isCorrect
                                    ? "bg-rose-100 text-rose-700"
                                    : "text-slate-600"
                                }`}
                              >
                                <span className={`mr-1.5 inline-flex h-4 w-4 items-center justify-center rounded-full text-[10px] font-bold ${opt === q.correctOption ? "bg-emerald-500 text-white" : opt === chosen && !isCorrect ? "bg-rose-400 text-white" : "bg-slate-200 text-slate-500"}`}>{opt}</span>
                                {q[`option${opt}`]}
                                {opt === q.correctOption && <span className="ml-1 text-emerald-700"> ✓ Correct</span>}
                                {opt === chosen && !isCorrect && <span className="ml-1 text-rose-600"> ✗ Your answer</span>}
                              </div>
                            ))}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}
            </div>
          )}
        </>
      )}
    </div>
  );
};

export const StudentProfilePage = () => {
  const sessionSnapshot = (() => {
    try {
      const raw = localStorage.getItem("pi_session");
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  })();
  const [profile, setProfile] = useState(null);
  const [profileApiMode, setProfileApiMode] = useState("modern");
  const [activeTab, setActiveTab] = useState("personal");
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    personalEmail: "",
    collegeEmail: "",
    mobileNumber: "",
    alternateMobileNumber: "",
    whatsappNumber: "",
    visibleToHr: true,
    addressLine1: "",
    addressLine2: "",
    city: "",
    state: "",
    pincode: "",
    dateOfBirth: "",
    gender: "",
    fatherName: "",
    fatherContactNumber: "",
    motherName: "",
    motherContactNumber: "",
    aboutMe: "",
    profileImageVisibleToHr: true,
    skills: [],
  });
  const [educationDraft, setEducationDraft] = useState({
    institutionName: "",
    degree: "",
    year: new Date().getFullYear(),
    cgpaOrPercentage: "",
  });
  const [editingEducationId, setEditingEducationId] = useState(null);
  const [newSkill, setNewSkill] = useState({
    skillName: "",
    skillCategory: "PROGRAMMING_LANGUAGE",
    skillLevel: "BEGINNER",
  });
  const [resumeFile, setResumeFile] = useState(null);
  const [profileImageFile, setProfileImageFile] = useState(null);
  const [resumePreviewUrl, setResumePreviewUrl] = useState("");
  const [profileImagePreviewUrl, setProfileImagePreviewUrl] = useState("");
  const [saving, setSaving] = useState(false);
  const [uploadingResume, setUploadingResume] = useState(false);
  const [uploadingImage, setUploadingImage] = useState(false);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const makeFallbackProfile = (source = {}) => ({
    studentId: source.studentId || null,
    studentName: source.studentName || sessionSnapshot?.name || "Student",
    role: "Student",
    personalInfo: {
      firstName: source.personalInfo?.firstName || (sessionSnapshot?.name || "").split(" ")[0] || "",
      lastName: source.personalInfo?.lastName || "",
      personalEmail: source.personalInfo?.personalEmail || sessionSnapshot?.email || "",
      collegeEmail: source.personalInfo?.collegeEmail || sessionSnapshot?.email || "",
      mobileNumber: source.personalInfo?.mobileNumber || "",
      alternateMobileNumber: source.personalInfo?.alternateMobileNumber || "",
      whatsappNumber: source.personalInfo?.whatsappNumber || "",
      visibleToHr: source.personalInfo?.visibleToHr ?? true,
    },
    address: {
      addressLine1: source.address?.addressLine1 || "",
      addressLine2: source.address?.addressLine2 || "",
      city: source.address?.city || "",
      state: source.address?.state || "",
      pincode: source.address?.pincode || "",
    },
    demographic: {
      dateOfBirth: source.demographic?.dateOfBirth || "",
      age: source.demographic?.age ?? null,
      gender: source.demographic?.gender || "",
    },
    parentInfo: {
      fatherName: source.parentInfo?.fatherName || "",
      fatherContactNumber: source.parentInfo?.fatherContactNumber || "",
      motherName: source.parentInfo?.motherName || "",
      motherContactNumber: source.parentInfo?.motherContactNumber || "",
    },
    about: {
      content: source.about?.content || "",
      characterCount: (source.about?.content || "").length,
      maxCharacters: 2000,
    },
    educationHistory: source.educationHistory || [],
    resume: source.resume || { uploaded: false, fileName: "", previewUrl: null },
    profileImage: source.profileImage || { uploaded: false, previewUrl: null, visibleToHr: true },
    skills: source.skills || [],
    analytics: source.analytics || {
      testScores: 0,
      averageTimePerQuestion: 0,
      accuracyPercentage: 0,
      strengths: "Keep your profile complete to improve visibility.",
      weaknesses: "Backend analytics unavailable while offline.",
      insightSummary: "Start backend on port 8080 to load live profile analytics.",
    },
    learningInsights: source.learningInsights || {
      recommendedLearningStrategy: "Complete profile sections and practice consistently.",
      weakAreas: "Live weak-area analysis is unavailable while offline.",
      suggestedTopics: "Aptitude, coding fundamentals, and communication.",
    },
  });

  useEffect(() => () => {
    if (resumePreviewUrl) URL.revokeObjectURL(resumePreviewUrl);
    if (profileImagePreviewUrl) URL.revokeObjectURL(profileImagePreviewUrl);
  }, [resumePreviewUrl, profileImagePreviewUrl]);

  const hydrateForm = (data) => {
    setForm({
      firstName: data.personalInfo?.firstName || "",
      lastName: data.personalInfo?.lastName || "",
      personalEmail: data.personalInfo?.personalEmail || "",
      collegeEmail: data.personalInfo?.collegeEmail || "",
      mobileNumber: data.personalInfo?.mobileNumber || "",
      alternateMobileNumber: data.personalInfo?.alternateMobileNumber || "",
      whatsappNumber: data.personalInfo?.whatsappNumber || "",
      visibleToHr: Boolean(data.personalInfo?.visibleToHr),
      addressLine1: data.address?.addressLine1 || "",
      addressLine2: data.address?.addressLine2 || "",
      city: data.address?.city || "",
      state: data.address?.state || "",
      pincode: data.address?.pincode || "",
      dateOfBirth: data.demographic?.dateOfBirth || "",
      gender: data.demographic?.gender || "",
      fatherName: data.parentInfo?.fatherName || "",
      fatherContactNumber: data.parentInfo?.fatherContactNumber || "",
      motherName: data.parentInfo?.motherName || "",
      motherContactNumber: data.parentInfo?.motherContactNumber || "",
      aboutMe: data.about?.content || "",
      profileImageVisibleToHr: Boolean(data.profileImage?.visibleToHr),
      skills: data.skills || [],
    });
  };

  const loadBinaryPreview = async (path, setter, contentTypeFallback) => {
    try {
      const response = await api.get(path, { responseType: "blob" });
      const type = response.data?.type || contentTypeFallback;
      const blob = new Blob([response.data], { type });
      const url = URL.createObjectURL(blob);
      setter(url);
    } catch {
      setter("");
    }
  };

  const load = async () => {
    setLoading(true);
    setError("");
    setMessage("");
    try {
      let mode = "modern";
      let data;
      try {
        const res = await api.get("/api/student/user-profile");
        data = res.data;
        mode = "modern";
      } catch {
        const legacy = await api.get("/api/student/profile");
        data = {
          studentId: legacy.data.studentId,
          studentName: legacy.data.name,
          role: "Student",
          personalInfo: {
            firstName: legacy.data.name || "",
            lastName: "",
            personalEmail: legacy.data.email || "",
            collegeEmail: legacy.data.email || "",
            mobileNumber: legacy.data.phone || "",
            alternateMobileNumber: "",
            whatsappNumber: "",
            visibleToHr: true,
          },
          address: {},
          demographic: { dateOfBirth: "", age: null, gender: "" },
          parentInfo: {},
          about: { content: legacy.data.achievements || "", characterCount: (legacy.data.achievements || "").length, maxCharacters: 2000 },
          educationHistory: [],
          resume: { uploaded: Boolean(legacy.data.resumeUploaded), fileName: legacy.data.resumeFileName || "", previewUrl: null },
          profileImage: { uploaded: false, previewUrl: null, visibleToHr: true },
          skills: (legacy.data.skills || []).map((name) => ({ id: `legacy-${name}`, skillName: name, skillCategory: "PROGRAMMING_LANGUAGE", skillLevel: "INTERMEDIATE" })),
          analytics: {
            testScores: legacy.data.profileStrength || 0,
            averageTimePerQuestion: 0,
            accuracyPercentage: 0,
            strengths: "Profile data available",
            weaknesses: "Detailed analytics not available in legacy mode",
            insightSummary: "Upgrade backend to enable full profile analytics.",
          },
          learningInsights: {
            recommendedLearningStrategy: "Practice daily and keep profile updated.",
            weakAreas: "Not available in legacy mode",
            suggestedTopics: "Aptitude, coding, communication",
          },
        };
        mode = "legacy";
      }
      setProfileApiMode(mode);
      setProfile(data);
      hydrateForm(data);
      localStorage.setItem("pi_profile_draft", JSON.stringify(data));
      if (data.resume?.uploaded && mode === "modern") {
        await loadBinaryPreview("/api/student/user-profile/resume/view", setResumePreviewUrl, "application/pdf");
      } else {
        setResumePreviewUrl("");
      }
      if (data.profileImage?.uploaded && mode === "modern") {
        await loadBinaryPreview("/api/student/user-profile/profile-image/view", setProfileImagePreviewUrl, "image/png");
      } else {
        setProfileImagePreviewUrl("");
      }
    } catch (e) {
      let cachedDraft = null;
      try {
        const raw = localStorage.getItem("pi_profile_draft");
        cachedDraft = raw ? JSON.parse(raw) : null;
      } catch {
        cachedDraft = null;
      }
      const fallback = makeFallbackProfile(cachedDraft || {});
      setProfileApiMode("offline");
      setProfile(fallback);
      hydrateForm(fallback);
      const networkError = !e.response;
      setError(networkError
        ? "Could not reach the API from this app session. Showing offline profile draft."
        : (e.response?.data?.message || "Failed to load profile."));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const validateProfile = () => {
    const checks = [
      { field: "personalEmail", rule: /^[^\s@]+@[^\s@]+\.[^\s@]+$/ },
      { field: "collegeEmail", rule: /^[^\s@]+@[^\s@]+\.[^\s@]+$/ },
      { field: "mobileNumber", rule: /^\d{10}$/ },
      { field: "alternateMobileNumber", rule: /^\d{10}$/ },
      { field: "whatsappNumber", rule: /^\d{10}$/ },
      { field: "fatherContactNumber", rule: /^\d{10}$/ },
      { field: "motherContactNumber", rule: /^\d{10}$/ },
    ];
    for (const item of checks) {
      const value = form[item.field];
      if (value && !item.rule.test(value)) return false;
    }
    if (form.pincode && !/^\d{6}$/.test(form.pincode)) return false;
    return true;
  };

  const save = async () => {
    if (!validateProfile()) {
      setError("Please correct email, phone, and pincode formats before saving.");
      return;
    }
    if ((form.aboutMe || "").length > 2000) {
      setError("About section must be at most 2000 characters.");
      return;
    }
    setSaving(true);
    setError("");
    setMessage("");
    try {
      let data;
      if (profileApiMode === "offline") {
        data = {
          ...profile,
          personalInfo: {
            ...profile.personalInfo,
            firstName: form.firstName,
            lastName: form.lastName,
            personalEmail: form.personalEmail,
            collegeEmail: form.collegeEmail,
            mobileNumber: form.mobileNumber,
            alternateMobileNumber: form.alternateMobileNumber,
            whatsappNumber: form.whatsappNumber,
            visibleToHr: form.visibleToHr,
          },
          address: {
            addressLine1: form.addressLine1,
            addressLine2: form.addressLine2,
            city: form.city,
            state: form.state,
            pincode: form.pincode,
          },
          demographic: {
            ...profile.demographic,
            dateOfBirth: form.dateOfBirth,
            gender: form.gender,
          },
          parentInfo: {
            fatherName: form.fatherName,
            fatherContactNumber: form.fatherContactNumber,
            motherName: form.motherName,
            motherContactNumber: form.motherContactNumber,
          },
          about: {
            ...profile.about,
            content: form.aboutMe,
            characterCount: (form.aboutMe || "").length,
          },
          profileImage: {
            ...profile.profileImage,
            visibleToHr: form.profileImageVisibleToHr,
          },
          skills: form.skills,
        };
      } else if (profileApiMode === "legacy") {
        const payload = {
          name: `${form.firstName || ""} ${form.lastName || ""}`.trim(),
          phone: form.mobileNumber || "",
          achievements: form.aboutMe || "",
          interests: form.aboutMe?.slice(0, 255) || "",
          level: "Intermediate",
          cgpa: 0,
        };
        await api.put("/api/student/profile", payload);
        const refreshed = await api.get("/api/student/profile");
        data = {
          ...profile,
          personalInfo: {
            ...profile.personalInfo,
            firstName: refreshed.data.name || form.firstName,
            personalEmail: refreshed.data.email || form.personalEmail,
            collegeEmail: refreshed.data.email || form.collegeEmail,
            mobileNumber: refreshed.data.phone || form.mobileNumber,
          },
          about: { ...profile.about, content: refreshed.data.achievements || form.aboutMe, characterCount: (refreshed.data.achievements || form.aboutMe || "").length },
        };
      } else {
        const payload = {
          ...form,
          skills: form.skills.map((s) => ({
            skillName: s.skillName,
            skillCategory: s.skillCategory,
            skillLevel: s.skillLevel,
          })),
        };
        const response = await api.put("/api/student/user-profile", payload);
        data = response.data;
      }
      setProfile(data);
      hydrateForm(data);
      if (profileApiMode === "offline") {
        localStorage.setItem("pi_profile_draft", JSON.stringify(data));
        if (isFirebaseConfigured()) {
          const session = readSessionSnapshot();
          await saveFirebaseRecord(`students/${session.studentId || "local"}/profileDraft`, data);
          setMessage("Offline draft saved locally and synced to Firebase.");
        } else {
          setMessage("Offline draft saved locally. Configure Firebase or start backend to sync profile.");
        }
      } else {
        setMessage("Profile updated successfully.");
      }
    } catch (e) {
      setError(e.response?.data?.message || "Failed to save profile.");
    } finally {
      setSaving(false);
    }
  };

  const uploadResume = async () => {
    if (profileApiMode === "offline") {
      setError("Resume upload needs backend connection. Start backend on port 8080.");
      return;
    }
    if (profileApiMode === "legacy") {
      setError("Resume upload requires the latest profile API. Restart backend with latest code.");
      return;
    }
    if (!resumeFile) {
      setError("Please select a resume PDF first.");
      return;
    }
    setUploadingResume(true);
    setError("");
    setMessage("");
    const formData = new FormData();
    formData.append("file", resumeFile);
    try {
      await api.post("/api/student/user-profile/resume", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      setMessage("Resume uploaded.");
      setResumeFile(null);
      await load();
    } catch (e) {
      setError(e.response?.data?.message || "Resume upload failed.");
    } finally {
      setUploadingResume(false);
    }
  };

  const deleteResume = async () => {
    if (profileApiMode === "offline") {
      setError("Resume delete needs backend connection. Start backend on port 8080.");
      return;
    }
    if (profileApiMode === "legacy") {
      setError("Resume delete requires the latest profile API.");
      return;
    }
    try {
      await api.delete("/api/student/user-profile/resume");
      setMessage("Resume deleted.");
      await load();
    } catch (e) {
      setError(e.response?.data?.message || "Failed to delete resume.");
    }
  };

  const uploadProfileImage = async () => {
    if (profileApiMode === "offline") {
      setError("Profile image upload needs backend connection. Start backend on port 8080.");
      return;
    }
    if (profileApiMode === "legacy") {
      setError("Profile image upload requires the latest profile API.");
      return;
    }
    if (!profileImageFile) {
      setError("Please select an image file first.");
      return;
    }
    setUploadingImage(true);
    setError("");
    setMessage("");
    const formData = new FormData();
    formData.append("file", profileImageFile);
    try {
      await api.post("/api/student/user-profile/profile-image", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      setMessage("Profile image uploaded.");
      setProfileImageFile(null);
      await load();
    } catch (e) {
      setError(e.response?.data?.message || "Profile image upload failed.");
    } finally {
      setUploadingImage(false);
    }
  };

  const deleteProfileImage = async () => {
    if (profileApiMode === "offline") {
      setError("Profile image delete needs backend connection. Start backend on port 8080.");
      return;
    }
    if (profileApiMode === "legacy") {
      setError("Profile image delete requires the latest profile API.");
      return;
    }
    try {
      await api.delete("/api/student/user-profile/profile-image");
      setMessage("Profile image deleted.");
      await load();
    } catch (e) {
      setError(e.response?.data?.message || "Failed to delete profile image.");
    }
  };

  const saveEducation = async () => {
    if (profileApiMode === "offline") {
      setError("Education save needs backend connection. Start backend on port 8080.");
      return;
    }
    if (profileApiMode === "legacy") {
      setError("Education CRUD requires the latest profile API.");
      return;
    }
    if (!educationDraft.institutionName || !educationDraft.degree || !educationDraft.year || !educationDraft.cgpaOrPercentage) {
      setError("All education fields are required.");
      return;
    }
    try {
      if (editingEducationId) {
        await api.put(`/api/student/user-profile/education/${editingEducationId}`, educationDraft);
        setMessage("Education updated.");
      } else {
        await api.post("/api/student/user-profile/education", educationDraft);
        setMessage("Education entry added.");
      }
      setEditingEducationId(null);
      setEducationDraft({
        institutionName: "",
        degree: "",
        year: new Date().getFullYear(),
        cgpaOrPercentage: "",
      });
      await load();
    } catch (e) {
      setError(e.response?.data?.message || "Failed to save education.");
    }
  };

  const editEducation = (entry) => {
    setEditingEducationId(entry.id);
    setEducationDraft({
      institutionName: entry.institutionName,
      degree: entry.degree,
      year: entry.year,
      cgpaOrPercentage: entry.cgpaOrPercentage,
    });
  };

  const deleteEducation = async (id) => {
    if (profileApiMode === "offline") {
      setError("Education delete needs backend connection. Start backend on port 8080.");
      return;
    }
    if (profileApiMode === "legacy") {
      setError("Education CRUD requires the latest profile API.");
      return;
    }
    try {
      await api.delete(`/api/student/user-profile/education/${id}`);
      setMessage("Education deleted.");
      await load();
    } catch (e) {
      setError(e.response?.data?.message || "Failed to delete education.");
    }
  };

  const addSkill = () => {
    if (!newSkill.skillName.trim()) return;
    setForm((prev) => ({
      ...prev,
      skills: [
        ...prev.skills,
        {
          id: `local-${Date.now()}`,
          skillName: newSkill.skillName.trim(),
          skillCategory: newSkill.skillCategory,
          skillLevel: newSkill.skillLevel,
        },
      ],
    }));
    setNewSkill({
      skillName: "",
      skillCategory: "PROGRAMMING_LANGUAGE",
      skillLevel: "BEGINNER",
    });
  };

  const removeSkill = (skillId, skillName) => {
    setForm((prev) => ({
      ...prev,
      skills: prev.skills.filter((item) => (skillId ? item.id !== skillId : item.skillName !== skillName)),
    }));
  };

  if (loading) return <p>Loading profile...</p>;
  if (error && !profile) return <p className="text-rose-600">{error}</p>;
  if (!profile) return <p>No profile data available.</p>;

  const tabs = [
    { key: "personal", label: "Personal Academic & Info" },
    { key: "skills", label: "Skills & Capabilities" },
  ];

  return (
    <div className="space-y-5">
      <section className="portal-banner">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-100">Adaptive Learning Strategy & Cognitive Intelligence System</p>
        <h2 className="mt-2 text-3xl font-bold text-white">User Profile Management Dashboard</h2>
        <p className="mt-2 text-sm text-cyan-50/90">Build a recruiter-visible profile, track performance analytics, and follow targeted learning insights.</p>
      </section>
      <div className="grid gap-4 md:grid-cols-4">
        <div className="dashboard-card p-5">
          <p className="text-sm text-slate-500">Test Scores</p>
          <p className="text-3xl font-semibold text-teal-700">{profile.analytics?.testScores || 0}%</p>
        </div>
        <div className="dashboard-card p-5">
          <p className="text-sm text-slate-500">Accuracy</p>
          <p className="mt-2 text-2xl font-semibold text-slate-900">{profile.analytics?.accuracyPercentage || 0}%</p>
        </div>
        <div className="dashboard-card p-5">
          <p className="text-sm text-slate-500">Avg Time / Question</p>
          <p className="mt-2 text-2xl font-semibold text-slate-900">{profile.analytics?.averageTimePerQuestion || 0}s</p>
        </div>
        <div className="dashboard-card p-5">
          <p className="text-sm text-slate-500">Role</p>
          <p className="mt-2 text-lg font-semibold text-slate-900">{profile.role}</p>
        </div>
      </div>
      <div className="dashboard-card p-4">
        <div className="flex flex-wrap gap-2">
          {tabs.map((tab) => (
            <button
              key={tab.key}
              type="button"
              onClick={() => setActiveTab(tab.key)}
              className={`rounded-xl px-4 py-2 text-sm font-semibold transition ${
                activeTab === tab.key
                  ? "bg-slate-900 text-white"
                  : "border border-slate-200 bg-white text-slate-700 hover:bg-slate-50"
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>
      {profileApiMode === "offline" ? (
        <div className="rounded-xl border border-sky-300 bg-sky-50 px-4 py-3 text-sm text-sky-900">
          Backend is unreachable. You are viewing an offline draft. Start backend with <code>gradlew.bat bootRun</code>, then refresh.
          {isFirebaseConfigured() ? " Firebase sync is available for saved drafts." : ""}
        </div>
      ) : null}
      {profileApiMode === "legacy" ? (
        <div className="rounded-xl border border-amber-300 bg-amber-50 px-4 py-3 text-sm text-amber-900">
          Running in compatibility mode. Full profile features need the latest backend runtime.
        </div>
      ) : null}
      {activeTab === "personal" ? (
        <div className="dashboard-card space-y-5 p-5">
          <div className="grid gap-3 md:grid-cols-2">
            <input className="rounded-xl border border-slate-200 px-3 py-2" value={form.firstName} onChange={(e) => setForm((prev) => ({ ...prev, firstName: e.target.value }))} placeholder="First Name" />
            <input className="rounded-xl border border-slate-200 px-3 py-2" value={form.lastName} onChange={(e) => setForm((prev) => ({ ...prev, lastName: e.target.value }))} placeholder="Last Name" />
            <input className="rounded-xl border border-slate-200 px-3 py-2" value={form.personalEmail} onChange={(e) => setForm((prev) => ({ ...prev, personalEmail: e.target.value }))} placeholder="Personal Email" />
            <input className="rounded-xl border border-slate-200 px-3 py-2" value={form.collegeEmail} onChange={(e) => setForm((prev) => ({ ...prev, collegeEmail: e.target.value }))} placeholder="College Email" />
            <input className="rounded-xl border border-slate-200 px-3 py-2" value={form.mobileNumber} onChange={(e) => setForm((prev) => ({ ...prev, mobileNumber: e.target.value }))} placeholder="Mobile Number" />
            <input className="rounded-xl border border-slate-200 px-3 py-2" value={form.alternateMobileNumber} onChange={(e) => setForm((prev) => ({ ...prev, alternateMobileNumber: e.target.value }))} placeholder="Alternate Mobile Number" />
            <input className="rounded-xl border border-slate-200 px-3 py-2" value={form.whatsappNumber} onChange={(e) => setForm((prev) => ({ ...prev, whatsappNumber: e.target.value }))} placeholder="WhatsApp Number" />
            <label className="flex items-center gap-2 rounded-xl border border-slate-200 px-3 py-2 text-sm">
              <input type="checkbox" checked={form.visibleToHr} onChange={(e) => setForm((prev) => ({ ...prev, visibleToHr: e.target.checked }))} />
              Visible to HR
            </label>
          </div>

          <h3 className="text-lg font-semibold text-slate-900">Address</h3>
          <div className="grid gap-3 md:grid-cols-2">
            <input className="rounded-xl border border-slate-200 px-3 py-2 md:col-span-2" value={form.addressLine1} onChange={(e) => setForm((prev) => ({ ...prev, addressLine1: e.target.value }))} placeholder="Address Line 1" />
            <input className="rounded-xl border border-slate-200 px-3 py-2 md:col-span-2" value={form.addressLine2} onChange={(e) => setForm((prev) => ({ ...prev, addressLine2: e.target.value }))} placeholder="Address Line 2" />
            <input className="rounded-xl border border-slate-200 px-3 py-2" value={form.city} onChange={(e) => setForm((prev) => ({ ...prev, city: e.target.value }))} placeholder="City" />
            <input className="rounded-xl border border-slate-200 px-3 py-2" value={form.state} onChange={(e) => setForm((prev) => ({ ...prev, state: e.target.value }))} placeholder="State" />
            <input className="rounded-xl border border-slate-200 px-3 py-2" value={form.pincode} onChange={(e) => setForm((prev) => ({ ...prev, pincode: e.target.value }))} placeholder="Pincode" />
          </div>

          <h3 className="text-lg font-semibold text-slate-900">Demographic Details</h3>
          <div className="grid gap-3 md:grid-cols-3">
            <input type="date" className="rounded-xl border border-slate-200 px-3 py-2" value={form.dateOfBirth} onChange={(e) => setForm((prev) => ({ ...prev, dateOfBirth: e.target.value }))} />
            <input className="rounded-xl border border-slate-200 px-3 py-2" value={profile.demographic?.age || ""} readOnly placeholder="Age (Auto)" />
            <select className="rounded-xl border border-slate-200 px-3 py-2" value={form.gender} onChange={(e) => setForm((prev) => ({ ...prev, gender: e.target.value }))}>
              <option value="">Gender (Optional)</option>
              <option value="Female">Female</option>
              <option value="Male">Male</option>
              <option value="Other">Other</option>
            </select>
          </div>

          <h3 className="text-lg font-semibold text-slate-900">Parent Information</h3>
          <div className="grid gap-3 md:grid-cols-2">
            <input className="rounded-xl border border-slate-200 px-3 py-2" value={form.fatherName} onChange={(e) => setForm((prev) => ({ ...prev, fatherName: e.target.value }))} placeholder="Father Name" />
            <input className="rounded-xl border border-slate-200 px-3 py-2" value={form.fatherContactNumber} onChange={(e) => setForm((prev) => ({ ...prev, fatherContactNumber: e.target.value }))} placeholder="Father Contact Number" />
            <input className="rounded-xl border border-slate-200 px-3 py-2" value={form.motherName} onChange={(e) => setForm((prev) => ({ ...prev, motherName: e.target.value }))} placeholder="Mother Name" />
            <input className="rounded-xl border border-slate-200 px-3 py-2" value={form.motherContactNumber} onChange={(e) => setForm((prev) => ({ ...prev, motherContactNumber: e.target.value }))} placeholder="Mother Contact Number" />
          </div>

          <h3 className="text-lg font-semibold text-slate-900">About</h3>
          <div>
            <textarea
              className="w-full rounded-xl border border-slate-200 px-3 py-2"
              rows={6}
              value={form.aboutMe}
              onChange={(e) => setForm((prev) => ({ ...prev, aboutMe: e.target.value.slice(0, 2000) }))}
              placeholder="Write your profile summary..."
              maxLength={2000}
            />
            <p className="mt-1 text-right text-xs text-slate-500">{(form.aboutMe || "").length}/2000</p>
          </div>

          <h3 className="text-lg font-semibold text-slate-900">Education & Career History</h3>
          <div className="grid gap-3 md:grid-cols-4">
            <input className="rounded-xl border border-slate-200 px-3 py-2" value={educationDraft.institutionName} onChange={(e) => setEducationDraft((prev) => ({ ...prev, institutionName: e.target.value }))} placeholder="Institution Name" />
            <input className="rounded-xl border border-slate-200 px-3 py-2" value={educationDraft.degree} onChange={(e) => setEducationDraft((prev) => ({ ...prev, degree: e.target.value }))} placeholder="Degree" />
            <input type="number" className="rounded-xl border border-slate-200 px-3 py-2" value={educationDraft.year} onChange={(e) => setEducationDraft((prev) => ({ ...prev, year: Number(e.target.value) }))} placeholder="Year" />
            <input className="rounded-xl border border-slate-200 px-3 py-2" value={educationDraft.cgpaOrPercentage} onChange={(e) => setEducationDraft((prev) => ({ ...prev, cgpaOrPercentage: e.target.value }))} placeholder="CGPA / Percentage" />
          </div>
          <div className="flex flex-wrap gap-2">
            <button type="button" onClick={saveEducation} className="rounded-xl bg-slate-900 px-4 py-2 text-white">
              {editingEducationId ? "Update Education" : "Add Education"}
            </button>
            {editingEducationId ? (
              <button
                type="button"
                onClick={() => {
                  setEditingEducationId(null);
                  setEducationDraft({ institutionName: "", degree: "", year: new Date().getFullYear(), cgpaOrPercentage: "" });
                }}
                className="rounded-xl border border-slate-300 px-4 py-2"
              >
                Cancel Edit
              </button>
            ) : null}
          </div>
          <div className="space-y-2">
            {(profile.educationHistory || []).map((entry) => (
              <div key={entry.id} className="flex flex-wrap items-center justify-between gap-2 rounded-xl border border-slate-200 bg-white p-3">
                <div className="text-sm">
                  <p className="font-semibold text-slate-900">{entry.institutionName} - {entry.degree}</p>
                  <p className="text-slate-600">{entry.year} | {entry.cgpaOrPercentage}</p>
                </div>
                <div className="flex gap-2">
                  <button type="button" onClick={() => editEducation(entry)} className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm">Edit</button>
                  <button type="button" onClick={() => deleteEducation(entry.id)} className="rounded-lg border border-rose-300 px-3 py-1.5 text-sm text-rose-700">Delete</button>
                </div>
              </div>
            ))}
          </div>

          <h3 className="text-lg font-semibold text-slate-900">Resume (PDF)</h3>
          <div className="flex flex-wrap items-center gap-3">
            <input type="file" accept=".pdf" className="rounded-xl border border-slate-200 px-3 py-2 text-sm" onChange={(e) => setResumeFile(e.target.files?.[0] || null)} />
            <button type="button" disabled={uploadingResume} onClick={uploadResume} className="rounded-xl bg-slate-900 px-4 py-2 text-white disabled:opacity-60">
              {uploadingResume ? "Uploading..." : "Upload / Replace Resume"}
            </button>
            <button type="button" onClick={deleteResume} className="rounded-xl border border-rose-300 px-4 py-2 text-rose-700">Delete Resume</button>
          </div>
          {resumePreviewUrl ? (
            <iframe title="Resume Preview" src={resumePreviewUrl} className="h-80 w-full rounded-xl border border-slate-200" />
          ) : <p className="text-sm text-slate-500">No resume uploaded.</p>}

          <h3 className="text-lg font-semibold text-slate-900">Profile Picture</h3>
          <div className="flex flex-wrap items-center gap-3">
            <input type="file" accept=".png,.jpg,.jpeg,.webp" className="rounded-xl border border-slate-200 px-3 py-2 text-sm" onChange={(e) => setProfileImageFile(e.target.files?.[0] || null)} />
            <button type="button" disabled={uploadingImage} onClick={uploadProfileImage} className="rounded-xl bg-slate-900 px-4 py-2 text-white disabled:opacity-60">
              {uploadingImage ? "Uploading..." : "Upload / Replace Image"}
            </button>
            <button type="button" onClick={deleteProfileImage} className="rounded-xl border border-rose-300 px-4 py-2 text-rose-700">Delete Image</button>
            <label className="flex items-center gap-2 rounded-xl border border-slate-200 px-3 py-2 text-sm">
              <input type="checkbox" checked={form.profileImageVisibleToHr} onChange={(e) => setForm((prev) => ({ ...prev, profileImageVisibleToHr: e.target.checked }))} />
              Image Visible to HR
            </label>
          </div>
          {profileImagePreviewUrl ? (
            <img src={profileImagePreviewUrl} alt="Profile Preview" className="h-32 w-32 rounded-2xl border border-slate-200 object-cover" />
          ) : <p className="text-sm text-slate-500">No profile image uploaded.</p>}
        </div>
      ) : null}
      {activeTab === "skills" ? (
        <div className="dashboard-card space-y-4 p-5">
          <h3 className="text-lg font-semibold text-slate-900">Skills & Capabilities</h3>
          <div className="grid gap-3 md:grid-cols-4">
            <input className="rounded-xl border border-slate-200 px-3 py-2 md:col-span-2" value={newSkill.skillName} onChange={(e) => setNewSkill((prev) => ({ ...prev, skillName: e.target.value }))} placeholder="Programming language or tool" />
            <select className="rounded-xl border border-slate-200 px-3 py-2" value={newSkill.skillCategory} onChange={(e) => setNewSkill((prev) => ({ ...prev, skillCategory: e.target.value }))}>
              <option value="PROGRAMMING_LANGUAGE">Programming Languages</option>
              <option value="TOOL_TECHNOLOGY">Tools / Technologies</option>
            </select>
            <select className="rounded-xl border border-slate-200 px-3 py-2" value={newSkill.skillLevel} onChange={(e) => setNewSkill((prev) => ({ ...prev, skillLevel: e.target.value }))}>
              <option value="BEGINNER">Beginner</option>
              <option value="INTERMEDIATE">Intermediate</option>
              <option value="ADVANCED">Advanced</option>
            </select>
          </div>
          <button type="button" onClick={addSkill} className="rounded-xl bg-slate-900 px-4 py-2 text-white">Add Skill</button>
          <div className="flex flex-wrap gap-2">
            {form.skills.map((skill) => (
              <div key={`${skill.id || skill.skillName}-${skill.skillLevel}`} className="flex items-center gap-2 rounded-full border border-slate-200 bg-slate-50 px-3 py-1.5 text-sm">
                <span className="font-semibold text-slate-800">{skill.skillName}</span>
                <span className="text-slate-500">{skill.skillCategory === "PROGRAMMING_LANGUAGE" ? "Language" : "Tool"} | {skill.skillLevel}</span>
                <button type="button" onClick={() => removeSkill(skill.id, skill.skillName)} className="text-rose-700">x</button>
              </div>
            ))}
          </div>
        </div>
      ) : null}
      {message ? <p className="text-sm text-emerald-700">{message}</p> : null}
      {error ? <p className="text-sm text-rose-600">{error}</p> : null}
      <button type="button" disabled={saving} onClick={save} className="rounded-xl bg-teal-700 px-4 py-2 text-white disabled:opacity-60">
        {saving ? "Saving..." : "Save Profile"}
      </button>
    </div>
  );
};

// Readiness ring rendered with a plain SVG circle — no external chart library needed
const ReadinessRing = ({ score }) => {
  const pct = Math.min(100, Math.max(0, score || 0));
  const radius = 52;
  const circumference = 2 * Math.PI * radius;
  const strokeDash = (pct / 100) * circumference;
  const color = pct >= 70 ? "#0f766e" : pct >= 40 ? "#d97706" : "#e11d48";
  return (
    <div className="flex flex-col items-center justify-center">
      <svg width="140" height="140" viewBox="0 0 140 140" aria-label={`Readiness score: ${pct}%`}>
        <circle cx="70" cy="70" r={radius} fill="none" stroke="#e2e8f0" strokeWidth="12" />
        <circle
          cx="70"
          cy="70"
          r={radius}
          fill="none"
          stroke={color}
          strokeWidth="12"
          strokeDasharray={`${strokeDash} ${circumference}`}
          strokeLinecap="round"
          transform="rotate(-90 70 70)"
        />
        <text x="70" y="66" textAnchor="middle" dominantBaseline="middle" fontSize="26" fontWeight="700" fill={color}>{pct}</text>
        <text x="70" y="88" textAnchor="middle" dominantBaseline="middle" fontSize="11" fill="#64748b">Readiness %</text>
      </svg>
    </div>
  );
};

// Manual SVG polyline chart for progress timeline — no recharts dependency here
const TimelineChart = ({ points }) => {
  if (!points || points.length === 0) return <p className="text-sm text-slate-500">No timeline data yet.</p>;
  const W = 480;
  const H = 160;
  const PAD = { top: 16, right: 16, bottom: 32, left: 36 };
  const minY = 0;
  const maxY = 100;
  const toX = (i) => PAD.left + (i / Math.max(points.length - 1, 1)) * (W - PAD.left - PAD.right);
  const toY = (v) => PAD.top + ((maxY - v) / (maxY - minY)) * (H - PAD.top - PAD.bottom);
  const polylinePoints = points.map((p, i) => `${toX(i)},${toY(p.readiness ?? 0)}`).join(" ");
  return (
    <svg viewBox={`0 0 ${W} ${H}`} className="w-full" aria-label="Readiness timeline">
      {/* Y gridlines */}
      {[0, 25, 50, 75, 100].map((v) => (
        <g key={v}>
          <line x1={PAD.left} x2={W - PAD.right} y1={toY(v)} y2={toY(v)} stroke="#e2e8f0" strokeWidth="1" />
          <text x={PAD.left - 4} y={toY(v)} textAnchor="end" dominantBaseline="middle" fontSize="9" fill="#94a3b8">{v}</text>
        </g>
      ))}
      {/* Line */}
      <polyline points={polylinePoints} fill="none" stroke="#0f766e" strokeWidth="2.5" strokeLinejoin="round" strokeLinecap="round" />
      {/* Dots + date labels */}
      {points.map((p, i) => (
        <g key={`${p.date}-${i}`}>
          <circle cx={toX(i)} cy={toY(p.readiness ?? 0)} r="4" fill="#0f766e" />
          {(i === 0 || i === points.length - 1 || points.length <= 6) && (
            <text x={toX(i)} y={H - PAD.bottom + 14} textAnchor="middle" fontSize="8" fill="#64748b">
              {(p.date || "").slice(5)}
            </text>
          )}
        </g>
      ))}
    </svg>
  );
};

export const StudentAnalyticsPage = () => {
  const [progress, setProgress] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    const loadProgress = async () => {
      setLoading(true);
      setError("");
      try {
        const res = await api.get("/api/student/progress");
        if (active) setProgress(res.data);
      } catch (e) {
        if (active) setError(e.response?.data?.message || "Failed to load analytics. Make sure the backend is running.");
      } finally {
        if (active) setLoading(false);
      }
    };
    loadProgress();
    return () => { active = false; };
  }, []);

  if (loading) return (
    <div className="flex items-center justify-center min-h-[400px]">
      <div className="text-center">
        <div className="inline-block h-12 w-12 animate-spin rounded-full border-4 border-solid border-teal-600 border-r-transparent" />
        <p className="mt-4 text-slate-600">Loading analytics...</p>
      </div>
    </div>
  );

  if (error) return (
    <div className="dashboard-card p-8 text-center">
      <svg className="mx-auto h-14 w-14 text-rose-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
      </svg>
      <p className="mt-4 text-lg font-semibold text-slate-900">Could not load analytics</p>
      <p className="mt-2 text-sm text-rose-600">{error}</p>
    </div>
  );

  if (!progress) return <p className="text-slate-600">No analytics data available.</p>;

  const section = progress.sectionPerformance || {};
  const aptitude = section.aptitude || 0;
  const coding = section.coding || 0;
  const softSkills = section.softSkills || 0;
  const readiness = Math.round(((aptitude + coding + softSkills) / 3) * 10) / 10;
  const timeline = Array.isArray(progress.timeline) ? progress.timeline : [];
  const suggestions = Array.isArray(progress.suggestions) ? progress.suggestions : [];

  // Derive weak topics from suggestions that mention specific areas
  const weakTopics = suggestions.filter((s) => s.toLowerCase().includes("improve") || s.toLowerCase().includes("practice"));
  const otherSuggestions = suggestions.filter((s) => !weakTopics.includes(s));

  const sectionBars = [
    { label: "Aptitude", value: aptitude, color: "bg-sky-500" },
    { label: "Coding (DSA)", value: coding, color: "bg-teal-500" },
    { label: "Soft Skills", value: softSkills, color: "bg-violet-500" },
  ];

  return (
    <div className="space-y-6">
      {/* Banner */}
      <section className="portal-banner">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-100">Personal Analytics</p>
        <h2 className="mt-2 text-3xl font-bold text-white">Skillora Performance Analytics</h2>
        <p className="mt-2 text-sm text-cyan-50/90">Track your readiness, section strengths, and weekly improvement across every practice area.</p>
      </section>

      {/* Readiness ring + stat cards */}
      <div className="grid gap-4 md:grid-cols-4">
        <div className="dashboard-card flex items-center justify-center p-5 md:row-span-1">
          <ReadinessRing score={readiness} />
        </div>
        <div className="dashboard-card p-5 text-center">
          <p className="text-sm text-slate-500">Aptitude</p>
          <p className="mt-1 text-3xl font-bold text-sky-600">{aptitude}%</p>
        </div>
        <div className="dashboard-card p-5 text-center">
          <p className="text-sm text-slate-500">Coding (DSA)</p>
          <p className="mt-1 text-3xl font-bold text-teal-600">{coding}%</p>
        </div>
        <div className="dashboard-card p-5 text-center">
          <p className="text-sm text-slate-500">Soft Skills</p>
          <p className="mt-1 text-3xl font-bold text-violet-600">{softSkills}%</p>
        </div>
      </div>

      {/* Section performance bars */}
      <div className="dashboard-card p-5">
        <p className="mb-4 text-lg font-semibold text-slate-900">Section Performance</p>
        <div className="space-y-4">
          {sectionBars.map(({ label, value, color }) => (
            <div key={label}>
              <div className="mb-1 flex justify-between text-sm">
                <span className="font-medium text-slate-700">{label}</span>
                <span className="font-semibold text-slate-900">{value}%</span>
              </div>
              <div className="h-3 w-full overflow-hidden rounded-full bg-slate-100">
                <div
                  className={`h-3 rounded-full transition-all duration-500 ${color}`}
                  style={{ width: `${value}%` }}
                />
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Progress timeline SVG chart */}
      <div className="dashboard-card p-5">
        <p className="mb-4 text-lg font-semibold text-slate-900">Readiness Progress Timeline</p>
        {timeline.length === 0 ? (
          <p className="text-sm text-slate-500">No timeline entries yet. Complete more tests to build your progress history.</p>
        ) : (
          <div className="overflow-x-auto">
            <TimelineChart points={timeline} />
          </div>
        )}
      </div>

      {/* Weak topics & suggestions */}
      <div className="grid gap-4 lg:grid-cols-2">
        {weakTopics.length > 0 && (
          <div className="dashboard-card p-5">
            <h3 className="text-lg font-semibold text-slate-900">Areas to Improve</h3>
            <ul className="mt-3 space-y-2">
              {weakTopics.map((item, idx) => (
                <li key={`${idx}-${item.slice(0, 12)}`} className="flex items-start gap-2 text-sm text-slate-700">
                  <span className="mt-0.5 flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-rose-100 text-xs font-bold text-rose-600">!</span>
                  {item}
                </li>
              ))}
            </ul>
          </div>
        )}
        <div className={`dashboard-card p-5 ${weakTopics.length === 0 ? "lg:col-span-2" : ""}`}>
          <h3 className="text-lg font-semibold text-slate-900">Recommendations</h3>
          {suggestions.length === 0 ? (
            <p className="mt-2 text-sm text-slate-500">No recommendations yet. Complete a test to generate personalised guidance.</p>
          ) : (
            <ul className="mt-3 space-y-2">
              {(otherSuggestions.length > 0 ? otherSuggestions : suggestions).map((item, idx) => (
                <li key={`${idx}-${item.slice(0, 12)}`} className="flex items-start gap-2 text-sm text-slate-700">
                  <span className="mt-0.5 flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-teal-100 text-xs font-bold text-teal-700">→</span>
                  {item}
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
};

// ─── Job Board Helpers ────────────────────────────────────────────────────────

const JOB_TYPE_COLORS = {
  FULL_TIME:  { bg: "bg-blue-50",   text: "text-blue-700",   border: "border-blue-200",  label: "Full Time" },
  INTERNSHIP: { bg: "bg-purple-50", text: "text-purple-700", border: "border-purple-200", label: "Internship" },
  CONTRACT:   { bg: "bg-orange-50", text: "text-orange-700", border: "border-orange-200", label: "Contract" },
  PART_TIME:  { bg: "bg-teal-50",   text: "text-teal-700",   border: "border-teal-200",   label: "Part Time" },
};

const STATUS_COLORS = {
  Open:       { bg: "bg-emerald-50",  text: "text-emerald-700",  dot: "bg-emerald-500"  },
  Applied:    { bg: "bg-sky-50",      text: "text-sky-700",      dot: "bg-sky-500"      },
  Interview:  { bg: "bg-amber-50",    text: "text-amber-700",    dot: "bg-amber-500"    },
  Offered:    { bg: "bg-green-50",    text: "text-green-700",    dot: "bg-green-500"    },
  Rejected:   { bg: "bg-red-50",      text: "text-red-600",      dot: "bg-red-400"      },
  Withdrawn:  { bg: "bg-slate-100",   text: "text-slate-500",    dot: "bg-slate-400"    },
};

const COMPANY_COLORS = [
  "#0061FF","#6B4FBB","#E44D26","#27AE60","#F07B00","#C0392B","#2980B9","#8E44AD",
];

function CompanyAvatar({ name, size = 40 }) {
  const idx = name ? name.charCodeAt(0) % COMPANY_COLORS.length : 0;
  return (
    <div style={{ width: size, height: size, background: COMPANY_COLORS[idx], borderRadius: 10, display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
      <span style={{ color: "#fff", fontWeight: 700, fontSize: size * 0.38 }}>
        {(name || "?").charAt(0).toUpperCase()}
      </span>
    </div>
  );
}

function SkillTag({ skill }) {
  return (
    <span className="inline-block rounded-full border border-slate-200 bg-slate-50 px-2.5 py-0.5 text-[11px] font-medium text-slate-600">
      {skill.trim()}
    </span>
  );
}

function JobCard({ job, onApply, onSave, submitting }) {
  const typeStyle = JOB_TYPE_COLORS[job.type] || JOB_TYPE_COLORS.FULL_TIME;
  const statusStyle = STATUS_COLORS[job.status] || STATUS_COLORS.Open;
  const skills = (job.requiredSkills || "").split(/[,;]/).filter(Boolean).slice(0, 4);
  const postedDays = job.postedAt ? Math.floor((Date.now() - new Date(job.postedAt)) / 86400000) : null;

  return (
    <div className="group rounded-2xl border border-slate-100 bg-white p-5 shadow-sm transition-all duration-200 hover:border-blue-200 hover:shadow-md">
      {/* Header Row */}
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-start gap-3">
          <CompanyAvatar name={job.company} size={46} />
          <div>
            <h3 className="text-base font-bold text-slate-900 group-hover:text-blue-700 transition-colors">{job.role}</h3>
            <p className="text-sm font-medium text-slate-600">{job.company}</p>
            <div className="mt-1 flex flex-wrap items-center gap-2 text-xs text-slate-500">
              {job.location && (
                <span className="flex items-center gap-1">
                  <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" /><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" /></svg>
                  {job.location}
                </span>
              )}
              {job.ctc && job.ctc !== "As per company standards" && (
                <span className="flex items-center gap-1">
                  <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                  {job.ctc}
                </span>
              )}
              {postedDays !== null && (
                <span>{postedDays === 0 ? "Today" : `${postedDays}d ago`}</span>
              )}
            </div>
          </div>
        </div>

        {/* Status + Save */}
        <div className="flex flex-col items-end gap-2 shrink-0">
          <div className={`flex items-center gap-1.5 rounded-full px-2.5 py-1 text-[11px] font-semibold ${statusStyle.bg} ${statusStyle.text}`}>
            <span className={`h-1.5 w-1.5 rounded-full ${statusStyle.dot}`} />
            {job.status}
          </div>
          <button
            onClick={() => onSave(job.id)}
            className="text-slate-400 hover:text-amber-500 transition-colors"
            title={job.saved ? "Unsave job" : "Save job"}
          >
            {job.saved
              ? <svg className="w-5 h-5 text-amber-500" fill="currentColor" viewBox="0 0 24 24"><path d="M5 3a2 2 0 00-2 2v16l7-3 7 3V5a2 2 0 00-2-2H5z"/></svg>
              : <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3-7 3V5z"/></svg>
            }
          </button>
        </div>
      </div>

      {/* Tags Row */}
      <div className="mt-3 flex flex-wrap gap-2">
        <span className={`rounded-full border px-2.5 py-0.5 text-[11px] font-semibold ${typeStyle.bg} ${typeStyle.text} ${typeStyle.border}`}>
          {typeStyle.label}
        </span>
        {job.requiredCgpa && (
          <span className="rounded-full border border-slate-200 bg-slate-50 px-2.5 py-0.5 text-[11px] font-medium text-slate-600">
            CGPA ≥ {job.requiredCgpa}
          </span>
        )}
        {skills.map((s) => <SkillTag key={s} skill={s} />)}
      </div>

      {/* Description snippet */}
      {job.description && (
        <p className="mt-3 text-xs leading-relaxed text-slate-500 line-clamp-2">{job.description}</p>
      )}

      {/* Footer row */}
      <div className="mt-4 flex items-center justify-between border-t border-slate-50 pt-3">
        <div className="text-xs text-slate-400">
          {job.recruiterName && `Posted by ${job.recruiterName}`}
          {job.appliedAt && ` • Applied ${new Date(job.appliedAt).toLocaleDateString()}`}
          {job.interviewAt && (
            <span className="ml-2 font-semibold text-amber-600">
              🗓 Interview: {new Date(job.interviewAt).toLocaleString()}
            </span>
          )}
        </div>
        <div className="flex gap-2">
          {job.status === "Open" && !job.applied && (
            <button
              onClick={() => onApply(job.id)}
              disabled={submitting === job.id}
              className="rounded-xl bg-blue-600 px-4 py-1.5 text-[13px] font-semibold text-white transition hover:bg-blue-700 disabled:opacity-60"
            >
              {submitting === job.id ? "Applying…" : "Quick Apply"}
            </button>
          )}
          {job.applied && (
            <span className="rounded-xl bg-slate-100 px-4 py-1.5 text-[13px] font-semibold text-slate-500">
              ✓ Applied
            </span>
          )}
        </div>
      </div>
    </div>
  );
}

export const StudentJobsDashboardPage = () => {
  const [jobs, setJobs] = useState([]);
  const [applications, setApplications] = useState([]);
  const [savedJobs, setSavedJobs] = usePersistentState("student_jobs_saved_v2", []);
  const [activeTab, setActiveTab] = useState("all"); // all | applied | saved
  const [filters, setFilters] = useState({ type: "All", status: "All", query: "" });
  const [loading, setLoading] = useState(true);
  const [submittingId, setSubmittingId] = useState(null);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const statusFromApplication = (application) => {
    if (!application) return "Open";
    const state = (application.status || "").toUpperCase();
    if (state === "INTERVIEW_SCHEDULED") return "Interview";
    if (state === "OFFERED") return "Offered";
    if (state === "REJECTED") return "Rejected";
    if (state === "WITHDRAWN") return "Withdrawn";
    return "Applied";
  };

  const loadData = async () => {
    setLoading(true);
    setError("");
    try {
      const [jobsRes, applicationsRes] = await Promise.all([
        api.get("/api/portal/student/jobs"),
        api.get("/api/portal/student/applications"),
      ]);
      setJobs(Array.isArray(jobsRes.data) ? jobsRes.data : []);
      setApplications(Array.isArray(applicationsRes.data) ? applicationsRes.data : []);
    } catch (e) {
      setError(e.response?.data?.message || "Failed to load jobs dashboard.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadData(); }, []);

  const applicationByJobId = useMemo(() => {
    const map = {};
    applications.forEach((app) => { map[app.jobId] = app; });
    return map;
  }, [applications]);

  const jobsView = useMemo(() => jobs.map((job) => {
    const application = applicationByJobId[job.jobId];
    return {
      ...job,
      id: job.jobId,
      role: job.title,
      company: job.companyName || "Skillora Partner",
      ctc: job.compensation || "As per company standards",
      type: job.jobType || "FULL_TIME",
      status: statusFromApplication(application),
      applied: Boolean(application),
      interviewAt: application?.interviewAt || null,
      appliedAt: application?.appliedAt || null,
      saved: savedJobs.includes(job.jobId),
    };
  }), [jobs, applicationByJobId, savedJobs]);

  const filteredJobs = useMemo(() => {
    let base = jobsView;
    if (activeTab === "applied") base = base.filter((j) => j.applied);
    else if (activeTab === "saved") base = base.filter((j) => j.saved);
    return base.filter((job) => {
      const typeOk = filters.type === "All" || job.type === filters.type;
      const statusOk = filters.status === "All" || job.status === filters.status;
      const queryOk = !filters.query
        || `${job.company} ${job.role} ${job.location || ""} ${job.requiredSkills || ""}`.toLowerCase().includes(filters.query.toLowerCase());
      return typeOk && statusOk && queryOk;
    });
  }, [filters, jobsView, activeTab]);

  const pipeline = useMemo(() => ({
    total: jobsView.length,
    applied: jobsView.filter((j) => j.applied).length,
    interview: jobsView.filter((j) => j.status === "Interview").length,
    offered: jobsView.filter((j) => j.status === "Offered").length,
  }), [jobsView]);

  const toggleSave = (id) => {
    setSavedJobs((prev) => (prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]));
  };

  const applyNow = async (id) => {
    setSubmittingId(id);
    setError("");
    setMessage("");
    try {
      await api.post(`/api/portal/student/jobs/${id}/apply`);
      setMessage("🎉 Application submitted successfully!");
      await loadData();
    } catch (e) {
      setError(e.response?.data?.message || "Failed to apply. Please try again.");
    } finally {
      setSubmittingId(null);
    }
  };

  return (
    <div className="space-y-6">
      {/* Hero Banner */}
      <div style={{background: "linear-gradient(135deg, #1a1f6e 0%, #3b4fc4 60%, #6c63ff 100%)"}} className="rounded-2xl p-6 text-white">
        <p className="text-xs font-semibold uppercase tracking-widest text-blue-200">Placement Opportunities</p>
        <h2 className="mt-1 text-2xl font-bold">Find Your Dream Job 🚀</h2>
        <p className="mt-1 text-sm text-blue-100">Explore campus placements, internships, and job offers tailored for you.</p>

        {/* Search bar */}
        <div className="mt-4 flex gap-2">
          <div className="flex flex-1 items-center gap-2 rounded-xl bg-white/10 border border-white/20 px-4 py-2 backdrop-blur-sm">
            <svg className="h-4 w-4 text-white/60" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <input
              className="flex-1 bg-transparent text-sm text-white placeholder-white/60 outline-none"
              placeholder="Search roles, companies, skills…"
              value={filters.query}
              onChange={(e) => setFilters((f) => ({ ...f, query: e.target.value }))}
            />
          </div>
          <button onClick={loadData} className="rounded-xl bg-white/20 border border-white/30 px-4 py-2 text-sm font-semibold text-white hover:bg-white/30 transition">
            Refresh
          </button>
        </div>
      </div>

      {/* Pipeline Stats */}
      <div className="grid grid-cols-2 gap-3 md:grid-cols-4">
        {[
          { label: "Total Openings", value: pipeline.total, color: "bg-blue-50 border-blue-100", textColor: "text-blue-700", icon: "💼" },
          { label: "Applied",        value: pipeline.applied, color: "bg-sky-50 border-sky-100",  textColor: "text-sky-700",  icon: "📋" },
          { label: "Interviews",     value: pipeline.interview, color: "bg-amber-50 border-amber-100", textColor: "text-amber-700", icon: "🗓" },
          { label: "Offers",         value: pipeline.offered, color: "bg-green-50 border-green-100", textColor: "text-green-700", icon: "🎉" },
        ].map((stat) => (
          <div key={stat.label} className={`rounded-2xl border p-4 ${stat.color}`}>
            <p className="text-xl">{stat.icon}</p>
            <p className={`mt-1 text-2xl font-bold ${stat.textColor}`}>{stat.value}</p>
            <p className="text-xs font-medium text-slate-500">{stat.label}</p>
          </div>
        ))}
      </div>

      {/* Tabs + Filters Row */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        {/* Tabs */}
        <div className="flex gap-1 rounded-xl bg-slate-100 p-1">
          {[
            { id: "all",     label: "All Jobs" },
            { id: "applied", label: `Applied (${pipeline.applied})` },
            { id: "saved",   label: `Saved (${savedJobs.length})` },
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`rounded-lg px-4 py-1.5 text-sm font-semibold transition ${activeTab === tab.id ? "bg-white text-slate-900 shadow-sm" : "text-slate-500 hover:text-slate-800"}`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* Filters */}
        <div className="flex flex-wrap gap-2">
          <select
            className="rounded-xl border border-slate-200 bg-white px-3 py-1.5 text-sm text-slate-700 shadow-sm outline-none"
            value={filters.type}
            onChange={(e) => setFilters((f) => ({ ...f, type: e.target.value }))}
          >
            <option value="All">All Types</option>
            <option value="FULL_TIME">Full Time</option>
            <option value="INTERNSHIP">Internship</option>
            <option value="CONTRACT">Contract</option>
            <option value="PART_TIME">Part Time</option>
          </select>
          <select
            className="rounded-xl border border-slate-200 bg-white px-3 py-1.5 text-sm text-slate-700 shadow-sm outline-none"
            value={filters.status}
            onChange={(e) => setFilters((f) => ({ ...f, status: e.target.value }))}
          >
            <option value="All">All Status</option>
            <option>Open</option>
            <option>Applied</option>
            <option>Interview</option>
            <option>Offered</option>
            <option>Rejected</option>
          </select>
        </div>
      </div>

      {/* Messages */}
      {message && (
        <div className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-700">
          {message}
        </div>
      )}
      {error && (
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-600">
          {error}
        </div>
      )}

      {/* Job Cards */}
      {loading ? (
        <div className="space-y-4">
          {[1, 2, 3].map((i) => (
            <div key={i} className="animate-pulse rounded-2xl border border-slate-100 bg-white p-5">
              <div className="flex gap-3">
                <div className="h-12 w-12 rounded-xl bg-slate-200" />
                <div className="flex-1 space-y-2">
                  <div className="h-4 w-1/3 rounded bg-slate-200" />
                  <div className="h-3 w-1/4 rounded bg-slate-200" />
                  <div className="h-3 w-1/2 rounded bg-slate-200" />
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : filteredJobs.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-slate-200 bg-slate-50 py-16 text-center">
          <span className="text-4xl">🔍</span>
          <p className="mt-3 text-base font-semibold text-slate-700">No jobs found</p>
          <p className="mt-1 text-sm text-slate-500">
            {activeTab === "saved" ? "You haven't saved any jobs yet." : "Try adjusting your filters or check back later."}
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          {filteredJobs.map((job) => (
            <JobCard
              key={job.id}
              job={job}
              onApply={applyNow}
              onSave={toggleSave}
              submitting={submittingId}
            />
          ))}
        </div>
      )}
    </div>
  );
};




const STUDENT_DEFAULT_TRACKS = [
  {
    id: 1,
    title: "Aptitude Speed Builder",
    section: "aptitude",
    route: "/student/aptitude",
    cta: "Start Aptitude Test",
    progress: 40,
    streak: 5,
    weakArea: "Probability",
    topic: "Permutations",
    expected: "20 adaptive aptitude questions with timer and instant scoring",
    icon: "📊",
    difficulty: "Mixed",
    duration: "20 min",
    color: "from-teal-600 to-emerald-500",
    subtopics: ["Percentages", "Profit & Loss", "HCF & LCM", "Probability"],
  },
  {
    id: 2,
    title: "Coding Interview Sprint",
    section: "coding",
    route: "/student/coding",
    cta: "Start Coding Test",
    progress: 62,
    streak: 3,
    weakArea: "Dynamic Programming",
    topic: "Binary Trees",
    expected: "DSA coding MCQs with code runner and submission evaluation",
    icon: "💻",
    difficulty: "Medium",
    duration: "60 min",
    color: "from-sky-600 to-blue-500",
    subtopics: ["Arrays", "Binary Trees", "Graphs", "DP"],
  },
  {
    id: 3,
    title: "Placement Mock Simulation",
    section: "mock",
    route: "/student/mock",
    cta: "Start Mock Test",
    progress: 28,
    streak: 2,
    weakArea: "Time management",
    topic: "Mixed aptitude + coding",
    expected: "Full 20 Aptitude + 2 Coding questions — campus drive simulation",
    icon: "🏆",
    difficulty: "Hard",
    duration: "90 min",
    color: "from-rose-600 to-pink-500",
    subtopics: ["Aptitude", "DSA", "Logical Reasoning", "Time management"],
  },
];

export const StudentLearningAcademyPage = () => {
  const navigate = useNavigate();
  const [tracks, setTracks] = usePersistentState("student_learning_tracks_v2", STUDENT_DEFAULT_TRACKS);
  const [activeTopic, setActiveTopic] = useState({});

  useEffect(() => {
    const hasLegacyShape = tracks.some((track) => !track.route || !track.cta || !track.expected || !track.icon);
    if (!hasLegacyShape) return;
    setTracks(STUDENT_DEFAULT_TRACKS);
  }, [tracks, setTracks]);

  const launchAutoTest = (id, route, topic) => {
    setTracks((prev) => prev.map((track) => (track.id === id
      ? { ...track, progress: Math.min(100, track.progress + 5), streak: track.streak + 1 }
      : track)));
    const target = topic ? `${route}?topic=${encodeURIComponent(topic)}` : route;
    navigate(target);
  };

  return (
    <div className="space-y-6">
      <section className="portal-banner">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-100">Learning Academy</p>
        <h2 className="mt-2 text-3xl font-bold text-white">🎓 Adaptive Learning Tracks</h2>
        <p className="mt-2 text-sm text-cyan-50/90">One-click AI-generated tests for each track. Every submission updates your readiness score and charts.</p>
        <div className="mt-4 flex flex-wrap gap-2">
          {["🎯 AI-Generated Questions", "⏱️ Real-time Timer", "📊 Instant Analytics", "🔁 Adaptive Difficulty"].map(tag => (
            <span key={tag} className="rounded-full bg-white/10 border border-white/20 px-3 py-1 text-xs text-white/90 font-medium">{tag}</span>
          ))}
        </div>
      </section>

      <div className="grid gap-5">
        {tracks.map((track) => (
          <div key={track.id} className="rounded-2xl border border-slate-200 bg-white overflow-hidden shadow-sm hover:shadow-md transition">
            {/* Track Header */}
            <div className={`bg-gradient-to-r ${track.color} p-5 flex items-center justify-between`}>
              <div className="flex items-center gap-3">
                <span className="text-3xl">{track.icon}</span>
                <div>
                  <p className="text-lg font-bold text-white">{track.title}</p>
                  <div className="flex gap-2 mt-1">
                    <span className="rounded-full bg-white/20 px-2 py-0.5 text-[10px] font-bold text-white">{track.difficulty}</span>
                    <span className="rounded-full bg-white/20 px-2 py-0.5 text-[10px] font-bold text-white">⏱ {track.duration}</span>
                    <span className="rounded-full bg-white/20 px-2 py-0.5 text-[10px] font-bold text-white">🔥 {track.streak} day streak</span>
                  </div>
                </div>
              </div>
              <button
                type="button"
                onClick={() => launchAutoTest(track.id, track.route, activeTopic[track.id] || track.topic)}
                className="rounded-xl bg-white px-4 py-2 text-sm font-bold shadow-sm transition hover:scale-105"
                style={{color: "#0f172a"}}
              >
                {track.cta} →
              </button>
            </div>

            {/* Track body */}
            <div className="p-5 grid md:grid-cols-[1fr_200px] gap-4">
              <div>
                <div className="flex items-center gap-2 mb-2">
                  <span className="text-xs font-semibold text-slate-500 uppercase tracking-wide">Weak Area:</span>
                  <span className="rounded-full bg-rose-100 text-rose-700 px-2 py-0.5 text-xs font-semibold">{track.weakArea}</span>
                </div>
                <p className="text-xs text-slate-500 mb-3">{track.expected}</p>

                {/* Subtopic quick-select */}
                <div>
                  <p className="text-xs font-semibold text-slate-500 uppercase mb-1.5">Jump to Topic:</p>
                  <div className="flex flex-wrap gap-1.5">
                    {track.subtopics.map(sub => (
                      <button
                        key={sub}
                        onClick={() => setActiveTopic(prev => ({...prev, [track.id]: sub}))}
                        className={`rounded-full px-2.5 py-1 text-xs font-medium transition ${
                          (activeTopic[track.id] || track.topic) === sub
                            ? "bg-slate-900 text-white"
                            : "bg-slate-100 text-slate-600 hover:bg-slate-200"
                        }`}
                      >
                        {sub}
                      </button>
                    ))}
                  </div>
                </div>
              </div>

              <div>
                <p className="text-xs font-semibold text-slate-500 uppercase mb-1">Progress</p>
                <div className="mb-1.5 flex justify-between text-sm">
                  <span className="font-medium text-slate-700">Overall</span>
                  <span className="font-bold text-slate-900">{track.progress}%</span>
                </div>
                <div className="h-2.5 w-full overflow-hidden rounded-full bg-slate-100">
                  <div className={`h-2.5 rounded-full bg-gradient-to-r ${track.color} transition-all duration-700`}
                    style={{ width: `${track.progress}%` }} />
                </div>
                <p className="mt-2 text-xs text-slate-500">Next: <strong className="text-slate-700">{activeTopic[track.id] || track.topic}</strong></p>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export const StudentReportsAnalyticsPage = () => {
  const [progress, setProgress] = useState(null);
  const [history, setHistory] = useState([]);
  const [testTypeFilter, setTestTypeFilter] = useState("ALL");
  const [windowFilter, setWindowFilter] = useState("ALL");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    Promise.all([api.get("/progress"), api.get("/api/student/profile")])
      .then(([progressRes, profileRes]) => {
        if (!active) return;
        setProgress(progressRes.data);
        setHistory(Array.isArray(profileRes.data?.history) ? profileRes.data.history : []);
      })
      .catch((e) => {
        if (active) setError(e.response?.data?.message || "Failed to load reports.");
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  const mockAttempts = history.filter((item) => item.testType === "MOCK");
  const allAttempts = [...history]
    .sort((a, b) => new Date(b.testDate || 0).getTime() - new Date(a.testDate || 0).getTime())
    .map((item) => {
      const total = item.totalQuestions || 0;
      const score = item.score || 0;
      const accuracy = total ? Math.round(((score * 100) / total) * 100) / 100 : 0;
      return { ...item, accuracy };
    });

  const cutoffDate = useMemo(() => {
    if (windowFilter === "ALL") return null;
    const days = Number(windowFilter);
    if (!Number.isFinite(days) || days <= 0) return null;
    const threshold = new Date();
    threshold.setDate(threshold.getDate() - days);
    return threshold;
  }, [windowFilter]);

  const filteredAttempts = allAttempts.filter((item) => {
    if (testTypeFilter !== "ALL" && item.testType !== testTypeFilter) return false;
    if (!cutoffDate || !item.testDate) return true;
    return new Date(item.testDate).getTime() >= cutoffDate.getTime();
  });

  const recentAttempts = filteredAttempts.slice(0, 10);

  const averageAccuracy = filteredAttempts.length
    ? Math.round((filteredAttempts.reduce((acc, item) => acc + item.accuracy, 0) / filteredAttempts.length) * 100) / 100
    : 0;
  const bestAccuracy = filteredAttempts.length ? Math.max(...filteredAttempts.map((item) => item.accuracy)) : 0;
  const readinessTimeline = progress?.timeline || [];
  const readinessVelocity = readinessTimeline.length >= 2
    ? Math.round((readinessTimeline[readinessTimeline.length - 1].readiness - readinessTimeline[0].readiness) * 100) / 100
    : 0;
  const projectedReadiness = readinessTimeline.length
    ? Math.max(0, Math.min(100, Math.round((readinessTimeline[readinessTimeline.length - 1].readiness + readinessVelocity) * 100) / 100))
    : 0;

  const accuracyTrend = [...filteredAttempts]
    .reverse()
    .map((item, index) => ({
      label: `${item.testType || "TEST"}-${index + 1}`,
      accuracy: item.accuracy,
    }));

  const typeDistribution = Object.entries(
    filteredAttempts.reduce((acc, item) => {
      const key = item.testType || "UNKNOWN";
      acc[key] = (acc[key] || 0) + 1;
      return acc;
    }, {}),
  ).map(([type, count]) => ({ type, count }));

  const sectionData = [
    { section: "Aptitude", value: progress?.sectionPerformance?.aptitude || 0 },
    { section: "Coding", value: progress?.sectionPerformance?.coding || 0 },
    { section: "Soft Skills", value: progress?.sectionPerformance?.softSkills || 0 },
  ];

  const downloadReportCsv = () => {
    downloadCsv(
      "student-analytics-report.csv",
      ["testDate", "testType", "score", "totalQuestions", "accuracy"],
      filteredAttempts.map((item) => [
        item.testDate || "",
        item.testType || "",
        item.score || 0,
        item.totalQuestions || 0,
        item.accuracy,
      ]),
    );
  };

  if (loading) return <p>Loading reports...</p>;
  if (error) return <p className="text-rose-600">{error}</p>;

  return (
    <div className="space-y-5">
      <section className="portal-banner">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-100">Reports & Analytics</p>
        <h2 className="mt-2 text-3xl font-bold text-white">Performance Intelligence Dashboard</h2>
        <p className="mt-2 text-sm text-cyan-50/90">Use filters, trend charts, and readiness velocity to plan your next improvement cycle.</p>
      </section>

      <div className="grid gap-4 md:grid-cols-4">
        <StatCard label="Attempts (Filtered)" value={filteredAttempts.length} />
        <StatCard label="Mock Attempts" value={mockAttempts.length} />
        <StatCard label="Average Accuracy" value={`${averageAccuracy}%`} />
        <StatCard label="Best Accuracy" value={`${bestAccuracy}%`} />
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <StatCard label="Readiness Velocity" value={`${readinessVelocity >= 0 ? "+" : ""}${readinessVelocity}`} helper="Change across the full timeline" />
        <StatCard label="Projected Readiness" value={`${projectedReadiness}%`} helper="Estimated from current trend" />
      </div>

      <div className="dashboard-card grid gap-3 p-4 md:grid-cols-4">
        <select className="rounded-xl border border-slate-200 px-3 py-2 text-sm" value={testTypeFilter} onChange={(e) => setTestTypeFilter(e.target.value)}>
          <option value="ALL">All Test Types</option>
          <option value="APTITUDE">Aptitude</option>
          <option value="CODING">Coding</option>
          <option value="SOFT_SKILLS">Soft Skills</option>
          <option value="MOCK">Mock</option>
        </select>
        <select className="rounded-xl border border-slate-200 px-3 py-2 text-sm" value={windowFilter} onChange={(e) => setWindowFilter(e.target.value)}>
          <option value="ALL">All Time</option>
          <option value="7">Last 7 Days</option>
          <option value="30">Last 30 Days</option>
          <option value="90">Last 90 Days</option>
        </select>
        <div className="md:col-span-2">
          <button type="button" onClick={downloadReportCsv} className="rounded-xl bg-slate-900 px-4 py-2 text-sm text-white">Download Filtered CSV</button>
        </div>
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        <div className="dashboard-card h-80 p-4 lg:col-span-2">
          <p className="mb-3 font-semibold">Readiness Timeline</p>
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={readinessTimeline}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="date" />
              <YAxis domain={[0, 100]} />
              <Tooltip />
              <Line dataKey="readiness" stroke="#0f766e" strokeWidth={3} />
            </LineChart>
          </ResponsiveContainer>
        </div>
        <div className="dashboard-card h-80 p-4">
          <p className="mb-3 font-semibold">Section Strength</p>
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={sectionData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="section" />
              <YAxis domain={[0, 100]} />
              <Tooltip />
              <Bar dataKey="value" fill="#155e75" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="grid gap-4 lg:grid-cols-2">
        <div className="dashboard-card h-80 p-4">
          <p className="mb-3 font-semibold">Accuracy Trend (Filtered)</p>
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={accuracyTrend}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="label" />
              <YAxis domain={[0, 100]} />
              <Tooltip />
              <Line dataKey="accuracy" stroke="#0284c7" strokeWidth={2.5} />
            </LineChart>
          </ResponsiveContainer>
        </div>
        <div className="dashboard-card h-80 p-4">
          <p className="mb-3 font-semibold">Attempt Distribution</p>
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={typeDistribution}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="type" />
              <YAxis />
              <Tooltip />
              <Bar dataKey="count" fill="#0f766e" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="dashboard-card p-4">
        <p className="font-semibold text-slate-900">Suggested Interventions</p>
        <div className="mt-3 space-y-2">
          <ProgressBar label="Aptitude" value={progress?.sectionPerformance?.aptitude || 0} />
          <ProgressBar label="Coding" value={progress?.sectionPerformance?.coding || 0} />
          <ProgressBar label="Soft Skills" value={progress?.sectionPerformance?.softSkills || 0} />
        </div>
        <ul className="mt-3 space-y-2 text-sm text-slate-700">
          {(progress?.suggestions || []).map((item) => <li key={item}>- {item}</li>)}
        </ul>
      </div>

      <div className="dashboard-card p-5">
        <h3 className="text-lg font-semibold text-slate-900">Recent Auto-Generated Tests (Filtered)</h3>
        {recentAttempts.length ? (
          <div className="mt-3 overflow-auto">
            <table className="min-w-full text-sm">
              <thead>
                <tr className="border-b border-slate-200 text-left text-slate-600">
                  <th className="px-2 py-2">Date</th>
                  <th className="px-2 py-2">Type</th>
                  <th className="px-2 py-2">Score</th>
                  <th className="px-2 py-2">Accuracy</th>
                </tr>
              </thead>
              <tbody>
                {recentAttempts.map((item) => (
                  <tr key={item.id} className="border-b border-slate-100 text-slate-700">
                    <td className="px-2 py-2">{item.testDate ? new Date(item.testDate).toLocaleDateString() : "-"}</td>
                    <td className="px-2 py-2">{item.testType || "-"}</td>
                    <td className="px-2 py-2">{item.score || 0}/{item.totalQuestions || 0}</td>
                    <td className="px-2 py-2">{item.accuracy}%</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="mt-2 text-sm text-slate-600">No attempts found for current filters. Change filters or start from Learning / Academy.</p>
        )}
      </div>
    </div>
  );
};

export const StudentTodoPage = () => {
  const [items, setItems] = usePersistentState("student_todo_v1", []);
  const [draft, setDraft] = useState({ title: "", dueDate: "", priority: "Medium" });

  const addTodo = () => {
    if (!draft.title.trim()) return;
    setItems((prev) => [...prev, { id: Date.now(), ...draft, done: false }]);
    setDraft({ title: "", dueDate: "", priority: "Medium" });
  };

  const toggleDone = (id) => setItems((prev) => prev.map((item) => (item.id === id ? { ...item, done: !item.done } : item)));
  const remove = (id) => setItems((prev) => prev.filter((item) => item.id !== id));
  const completed = items.filter((item) => item.done).length;
  const progress = items.length ? (completed / items.length) * 100 : 0;

  return (
    <div className="space-y-5">
      <section className="portal-banner">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-100">To-Do</p>
        <h2 className="mt-2 text-3xl font-bold text-white">Daily Execution Planner</h2>
        <p className="mt-2 text-sm text-cyan-50/90">Manage preparation tasks with due dates and priority levels.</p>
      </section>
      <div className="dashboard-card p-4">
        <ProgressBar label={`${completed}/${items.length} tasks completed`} value={progress} />
      </div>
      <div className="dashboard-card grid gap-3 p-4 md:grid-cols-4">
        <input className="rounded-xl border border-slate-200 px-3 py-2 text-sm md:col-span-2" placeholder="Task title" value={draft.title} onChange={(e) => setDraft((d) => ({ ...d, title: e.target.value }))} />
        <input type="date" className="rounded-xl border border-slate-200 px-3 py-2 text-sm" value={draft.dueDate} onChange={(e) => setDraft((d) => ({ ...d, dueDate: e.target.value }))} />
        <select className="rounded-xl border border-slate-200 px-3 py-2 text-sm" value={draft.priority} onChange={(e) => setDraft((d) => ({ ...d, priority: e.target.value }))}>
          <option>High</option><option>Medium</option><option>Low</option>
        </select>
        <button type="button" onClick={addTodo} className="rounded-xl bg-slate-900 px-3 py-2 text-sm text-white">Add Task</button>
      </div>
      <div className="space-y-2">
        {items.map((item) => (
          <div key={item.id} className="dashboard-card flex flex-wrap items-center justify-between gap-2 p-3">
            <label className="flex items-center gap-2 text-sm">
              <input type="checkbox" checked={item.done} onChange={() => toggleDone(item.id)} />
              <span className={item.done ? "line-through text-slate-400" : "text-slate-800"}>{item.title}</span>
            </label>
            <div className="flex items-center gap-2 text-xs text-slate-500">
              <span>{item.priority}</span>
              <span>{item.dueDate || "No deadline"}</span>
              <button type="button" onClick={() => remove(item.id)} className="rounded-lg border border-rose-300 px-2 py-1 text-rose-700">Delete</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export const StudentNotesPage = () => {
  const [notes, setNotes] = usePersistentState("student_notes_v1", []);
  const [draft, setDraft] = useState({ title: "", content: "", tags: "" });
  const [query, setQuery] = useState("");

  const addNote = () => {
    if (!draft.title.trim() && !draft.content.trim()) return;
    const parsedTags = draft.tags.split(",").map((tag) => tag.trim()).filter(Boolean);
    setNotes((prev) => [{ id: Date.now(), title: draft.title, content: draft.content, tags: parsedTags, pinned: false }, ...prev]);
    setDraft({ title: "", content: "", tags: "" });
  };

  const togglePin = (id) => setNotes((prev) => prev.map((note) => (note.id === id ? { ...note, pinned: !note.pinned } : note)));
  const remove = (id) => setNotes((prev) => prev.filter((note) => note.id !== id));

  const filtered = useMemo(() => {
    const sorted = [...notes].sort((a, b) => Number(b.pinned) - Number(a.pinned));
    if (!query.trim()) return sorted;
    return sorted.filter((note) => `${note.title} ${note.content} ${note.tags.join(" ")}`.toLowerCase().includes(query.toLowerCase()));
  }, [notes, query]);

  return (
    <div className="space-y-5">
      <section className="portal-banner">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-100">Notes</p>
        <h2 className="mt-2 text-3xl font-bold text-white">Smart Notes Workspace</h2>
        <p className="mt-2 text-sm text-cyan-50/90">Capture interview notes, topic shortcuts, and revision snippets with tags.</p>
      </section>
      <div className="dashboard-card grid gap-3 p-4 md:grid-cols-2">
        <input className="rounded-xl border border-slate-200 px-3 py-2 text-sm" placeholder="Title" value={draft.title} onChange={(e) => setDraft((d) => ({ ...d, title: e.target.value }))} />
        <input className="rounded-xl border border-slate-200 px-3 py-2 text-sm" placeholder="Tags (comma separated)" value={draft.tags} onChange={(e) => setDraft((d) => ({ ...d, tags: e.target.value }))} />
        <textarea className="rounded-xl border border-slate-200 px-3 py-2 text-sm md:col-span-2" rows={4} placeholder="Write your note..." value={draft.content} onChange={(e) => setDraft((d) => ({ ...d, content: e.target.value }))} />
        <button type="button" onClick={addNote} className="rounded-xl bg-slate-900 px-3 py-2 text-sm text-white">Add Note</button>
      </div>
      <div className="dashboard-card p-4">
        <input className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm" placeholder="Search notes by keyword or tag" value={query} onChange={(e) => setQuery(e.target.value)} />
      </div>
      <div className="grid gap-3">
        {filtered.map((note) => (
          <div key={note.id} className="dashboard-card p-4">
            <div className="flex items-center justify-between gap-2">
              <p className="font-semibold text-slate-900">{note.title || "Untitled Note"}</p>
              <div className="flex gap-2">
                <button type="button" onClick={() => togglePin(note.id)} className="rounded-lg border border-slate-300 px-2 py-1 text-xs">{note.pinned ? "Pinned" : "Pin"}</button>
                <button type="button" onClick={() => remove(note.id)} className="rounded-lg border border-rose-300 px-2 py-1 text-xs text-rose-700">Delete</button>
              </div>
            </div>
            <p className="mt-2 text-sm text-slate-700">{note.content}</p>
            <div className="mt-2 flex flex-wrap gap-2">
              {note.tags.map((tag) => <span key={`${note.id}-${tag}`} className="rounded-full bg-slate-100 px-2 py-1 text-xs text-slate-600">#{tag}</span>)}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

const splitLines = (value) =>
  value
    .split("\n")
    .map((item) => item.trim())
    .filter(Boolean);

const MentorMarkdown = ({ content }) => {
  if (!content) return null;
  return (
    <div className="space-y-3 text-sm leading-6 text-slate-700">
      {content.split("\n").map((line, index) => {
        const key = `${index}-${line.slice(0, 16)}`;
        if (!line.trim()) return <div key={key} className="h-1" />;
        if (line.startsWith("### ")) return <h4 key={key} className="pt-2 text-base font-semibold text-slate-900">{line.replace(/^### /, "")}</h4>;
        if (line.startsWith("## ")) return <h3 key={key} className="pt-4 text-xl font-bold text-slate-900">{line.replace(/^## /, "")}</h3>;
        if (line.startsWith("- ")) return <p key={key} className="pl-4">- {line.slice(2)}</p>;
        const html = line.replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>");
        return <p key={key} dangerouslySetInnerHTML={{ __html: html }} />;
      })}
    </div>
  );
};

const APTITUDE_CURRICULUM = [
  { topic: "Percentages", subtopics: ["Percentage increase/decrease", "Percentage of a number", "Successive %", "Error %"] },
  { topic: "Profit & Loss", subtopics: ["Cost price, selling price", "Markup & discount", "Dishonest dealings"] },
  { topic: "Time & Work", subtopics: ["Work rate", "Work efficiency", "Pipes & Cisterns"] },
  { topic: "Speed, Distance & Time", subtopics: ["Relative speed", "Trains & boats", "Average speed"] },
  { topic: "HCF & LCM", subtopics: ["GCD method", "LCM word problems", "Divisibility rules"] },
  { topic: "Probability", subtopics: ["Sample space", "Conditional probability", "Binomial"] },
  { topic: "Permutations & Combinations", subtopics: ["Arrangements", "Selections", "Circular permutation"] },
  { topic: "Number System", subtopics: ["Remainders", "Divisibility", "Prime factorization"] },
  { topic: "Averages", subtopics: ["Weighted average", "Replacing average", "Moving average"] },
  { topic: "Ratio & Proportion", subtopics: ["Direct ratio", "Inverse ratio", "Mixture problems"] },
];

const DSA_CURRICULUM = [
  { topic: "Arrays", subtopics: ["Two-pointer", "Sliding window", "Prefix sum", "Sorting"] },
  { topic: "Binary Trees", subtopics: ["Traversals", "BST operations", "Height & diameter"] },
  { topic: "Dynamic Programming", subtopics: ["1D DP", "2D DP", "LCS/LIS", "Coin change"] },
  { topic: "Graphs", subtopics: ["BFS/DFS", "Shortest path", "Cycle detection", "Topological sort"] },
  { topic: "Stacks & Queues", subtopics: ["Monotonic stack", "Deque", "Expression parsing"] },
  { topic: "Linked Lists", subtopics: ["Reversal", "Fast-slow pointer", "Merge sorted lists"] },
  { topic: "Searching", subtopics: ["Binary search", "Rotated arrays", "Search in 2D"] },
  { topic: "Sorting", subtopics: ["Merge sort", "Quick sort", "Heap sort", "Counting sort"] },
];

const AI_MODES = [
  { value: "LEARN", label: "Learn", icon: "📚", desc: "Structured notes + concept explanations", color: "from-sky-500 to-blue-500" },
  { value: "PRACTICE", label: "Practice", icon: "✏️", desc: "Practice questions on selected topic", color: "from-teal-500 to-emerald-500" },
  { value: "ADAPTIVE", label: "Adaptive", icon: "🧠", desc: "AI adapts to your weak areas", color: "from-purple-500 to-violet-500" },
  { value: "REVISION", label: "Revision", icon: "🔁", desc: "Quick revision & formula sheets", color: "from-amber-500 to-orange-500" },
  { value: "MOCK_TEST", label: "Mock Test", icon: "🏆", desc: "Full placement simulation test", color: "from-rose-500 to-pink-500" },
];

export const StudentAiMentorPage = () => {
  const [form, setForm] = usePersistentState("skillora_ai_mentor_form_v2", {
    mode: "LEARN",
    topic: "Percentages",
    subtopic: "Percentage increase and decrease",
    difficulty: "Adaptive",
    numberOfQuestions: 10,
    studentLevel: "Beginner",
    previousTopicsCovered: "Number System\nAverages",
    previouslyGeneratedQuestions: "",
    weakTopics: "Percentage base value\nTime management",
    correct: 6,
    wrong: 4,
    accuracy: 60,
    curriculumType: "APTITUDE",
  });
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const update = (field, value) => setForm((prev) => ({ ...prev, [field]: value }));

  const curriculum = form.curriculumType === "APTITUDE" ? APTITUDE_CURRICULUM : DSA_CURRICULUM;
  const selectedCurriculumItem = curriculum.find(c => c.topic === form.topic) || curriculum[0];

  const generate = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    setResult(null);
    try {
      const payload = {
        ...form,
        numberOfQuestions: Number(form.numberOfQuestions) || 10,
        correct: Number(form.correct) || 0,
        wrong: Number(form.wrong) || 0,
        accuracy: Number(form.accuracy) || 0,
        previousTopicsCovered: splitLines(form.previousTopicsCovered || ""),
        previouslyGeneratedQuestions: splitLines(form.previouslyGeneratedQuestions || ""),
        weakTopics: splitLines(form.weakTopics || ""),
      };
      const { data } = await api.post("/api/student/ai-mentor", payload);
      setResult(data);
    } catch (e) {
      setError(e.response?.data?.message || "Failed to generate mentor content.");
    } finally {
      setLoading(false);
    }
  };

  const selectedMode = AI_MODES.find(m => m.value === form.mode) || AI_MODES[0];

  return (
    <div className="space-y-6">
      <section className="portal-banner">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-100">Skillora AI</p>
        <h2 className="mt-2 text-3xl font-bold text-white">🤖 AI Placement Mentor</h2>
        <p className="mt-2 text-sm text-cyan-50/90">Groq-powered AI generates learning notes, adaptive practice, revision sheets, and mock tests personalized for you.</p>
        <div className="mt-4 flex flex-wrap gap-2">
          {["🤖 Groq Llama 3.3", "📚 Topic Navigator", "⚡ Adaptive AI", "🏆 Mock Test Mode"].map(tag => (
            <span key={tag} className="rounded-full bg-white/10 border border-white/20 px-3 py-1 text-xs text-white/90 font-medium">{tag}</span>
          ))}
        </div>
      </section>

      <form onSubmit={generate} className="space-y-5">
        {/* Mode Selector Cards */}
        <div className="dashboard-card p-5">
          <h3 className="mb-3 text-sm font-bold text-slate-900 uppercase tracking-wide">Select Mode</h3>
          <div className="grid grid-cols-2 gap-2 md:grid-cols-5">
            {AI_MODES.map(mode => (
              <button
                key={mode.value}
                type="button"
                onClick={() => update("mode", mode.value)}
                className={`rounded-xl p-3 text-left transition hover:scale-[1.02] ${
                  form.mode === mode.value
                    ? `bg-gradient-to-br ${mode.color} text-white shadow-md`
                    : "border border-slate-200 bg-slate-50 text-slate-700 hover:bg-slate-100"
                }`}
              >
                <p className="text-xl mb-1">{mode.icon}</p>
                <p className="text-xs font-bold">{mode.label}</p>
                <p className={`text-[10px] mt-0.5 ${form.mode === mode.value ? "text-white/80" : "text-slate-500"}`}>{mode.desc}</p>
              </button>
            ))}
          </div>
        </div>

        {/* Curriculum Navigator */}
        <div className="dashboard-card p-5">
          <div className="flex items-center justify-between mb-3">
            <h3 className="text-sm font-bold text-slate-900 uppercase tracking-wide">📚 Topic Curriculum</h3>
            <div className="flex gap-1 rounded-lg bg-slate-100 p-1">
              {["APTITUDE", "DSA"].map(type => (
                <button
                  key={type}
                  type="button"
                  onClick={() => { update("curriculumType", type); update("topic", type === "APTITUDE" ? "Percentages" : "Arrays"); }}
                  className={`rounded px-3 py-1 text-xs font-semibold transition ${
                    form.curriculumType === type ? "bg-white text-slate-900 shadow-sm" : "text-slate-500"
                  }`}
                >
                  {type === "APTITUDE" ? "📊 Aptitude" : "💻 DSA"}
                </button>
              ))}
            </div>
          </div>

          <div className="flex flex-wrap gap-1.5 mb-4">
            {curriculum.map(item => (
              <button
                key={item.topic}
                type="button"
                onClick={() => { update("topic", item.topic); update("subtopic", item.subtopics[0]); }}
                className={`rounded-full px-3 py-1.5 text-xs font-semibold transition ${
                  form.topic === item.topic
                    ? "bg-slate-900 text-white shadow-sm"
                    : "bg-slate-100 text-slate-600 hover:bg-slate-200"
                }`}
              >
                {item.topic}
              </button>
            ))}
          </div>

          {/* Subtopic chips */}
          {selectedCurriculumItem && (
            <div>
              <p className="text-xs font-semibold text-slate-500 uppercase mb-2">Sub-topics in {form.topic}:</p>
              <div className="flex flex-wrap gap-1.5">
                {selectedCurriculumItem.subtopics.map(sub => (
                  <button
                    key={sub}
                    type="button"
                    onClick={() => update("subtopic", sub)}
                    className={`rounded-full border px-2.5 py-1 text-xs font-medium transition ${
                      form.subtopic === sub
                        ? "border-teal-500 bg-teal-50 text-teal-700"
                        : "border-slate-200 bg-white text-slate-600 hover:border-teal-300"
                    }`}
                  >
                    {sub}
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Settings row */}
        <div className="dashboard-card p-5">
          <h3 className="mb-3 text-sm font-bold text-slate-900 uppercase tracking-wide">⚙️ Configuration</h3>
          <div className="grid gap-3 md:grid-cols-4">
            <label className="text-sm font-semibold text-slate-700">
              Questions
              <input className="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm" type="number" min="1" max="30" value={form.numberOfQuestions} onChange={(e) => update("numberOfQuestions", e.target.value)} />
            </label>
            <label className="text-sm font-semibold text-slate-700">
              Difficulty
              <select className="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm" value={form.difficulty} onChange={(e) => update("difficulty", e.target.value)}>
                <option>Adaptive</option>
                <option>Easy</option>
                <option>Medium</option>
                <option>Hard</option>
              </select>
            </label>
            <label className="text-sm font-semibold text-slate-700">
              Student Level
              <select className="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm" value={form.studentLevel} onChange={(e) => update("studentLevel", e.target.value)}>
                <option>Beginner</option>
                <option>Intermediate</option>
                <option>Advanced</option>
              </select>
            </label>
            <div className="grid grid-cols-3 gap-2">
              <label className="text-sm font-semibold text-slate-700">
                Correct
                <input className="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm" type="number" min="0" value={form.correct} onChange={(e) => update("correct", e.target.value)} />
              </label>
              <label className="text-sm font-semibold text-slate-700">
                Wrong
                <input className="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm" type="number" min="0" value={form.wrong} onChange={(e) => update("wrong", e.target.value)} />
              </label>
              <label className="text-sm font-semibold text-slate-700">
                Acc %
                <input className="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm" type="number" min="0" max="100" value={form.accuracy} onChange={(e) => update("accuracy", e.target.value)} />
              </label>
            </div>
          </div>

          <div className="mt-4 grid gap-3 lg:grid-cols-2">
            <label className="text-sm font-semibold text-slate-700">
              Weak Topics (one per line)
              <textarea className="mt-1 h-20 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm" value={form.weakTopics} onChange={(e) => update("weakTopics", e.target.value)} />
            </label>
            <label className="text-sm font-semibold text-slate-700">
              Previous Topics Covered
              <textarea className="mt-1 h-20 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm" value={form.previousTopicsCovered} onChange={(e) => update("previousTopicsCovered", e.target.value)} />
            </label>
          </div>
        </div>

        {/* CTA + Summary */}
        <div className="flex items-center gap-4">
          <button
            type="submit"
            disabled={loading}
            className={`flex items-center gap-2 rounded-xl px-8 py-3 font-bold text-white shadow-lg transition hover:scale-[1.02] disabled:opacity-60 ${
              loading ? "bg-slate-400" : `bg-gradient-to-r ${selectedMode.color}`
            }`}
          >
            {loading ? (
              <><span className="animate-spin text-lg">⚙️</span> Generating...</>
            ) : (
              <><span>{selectedMode.icon}</span> Generate {selectedMode.label} Content</>
            )}
          </button>
          <div className="text-sm text-slate-500">
            <span className="font-medium text-slate-700">{form.topic}</span> → <span>{form.subtopic}</span>
          </div>
        </div>

        {error && (
          <div className="rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-600">
            ⚠️ {error}
          </div>
        )}
      </form>

      {result && (
        <section className="dashboard-card overflow-hidden">
          <div className="border-b border-slate-100 p-5">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <div>
                <p className="text-xs font-semibold uppercase tracking-[0.16em] text-teal-700">{result.mode} | {result.topic}</p>
                <h3 className="text-xl font-bold text-slate-900">AI-Generated Guidance</h3>
                <p className="text-sm text-slate-500">{result.subtopic}</p>
              </div>
              <div className="flex gap-2">
                <span className={`rounded-full px-3 py-1 text-xs font-semibold ${
                  result.aiGenerated ? "bg-emerald-100 text-emerald-800" : "bg-amber-100 text-amber-800"
                }`}>
                  {result.aiGenerated ? "🤖 AI Generated" : "📦 Offline Template"}
                </span>
              </div>
            </div>
          </div>
          <div className="p-5">
            <MentorMarkdown content={result.content} />
          </div>
        </section>
      )}
    </div>
  );
};
