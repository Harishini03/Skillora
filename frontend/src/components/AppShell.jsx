import { useEffect, useRef, useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/useAuth";
import api from "../lib/api";

// ─── Nav config with icons ────────────────────────────────────────────────

const studentNav = [
  { to: "/student/dashboard",  label: "Dashboard",       icon: "⊞" },
  { to: "/student/profile",    label: "My Profile",       icon: "👤" },
  { to: "/student/aptitude",   label: "Aptitude Tests",   icon: "📝" },
  { to: "/student/dsa",        label: "DSA Practice",     icon: "💻" },
  { to: "/student/ai-mentor",  label: "AI Mentor",        icon: "🤖" },
  { to: "/student/mock",       label: "Mock Test",        icon: "🏆" },
  { to: "/student/jobs",       label: "Job Board",        icon: "💼" },
  { to: "/student/analytics",  label: "Analytics",        icon: "📊" },
];

const recruiterNav = [
  { to: "/recruiter/dashboard",  label: "Dashboard",         icon: "⊞" },
  { to: "/recruiter/top",        label: "Top Talent",        icon: "🏅" },
  { to: "/recruiter/monitoring", label: "Candidate Monitor", icon: "🔍" },
];

const staffNav = [
  { to: "/staff/dashboard",  label: "Command Center",    icon: "⊞" },
  { to: "/staff/talent",     label: "Talent Intelligence", icon: "🧠" },
  { to: "/staff/monitoring", label: "Student Monitor",   icon: "📡" },
];

const NAV_BY_ROLE = { STUDENT: studentNav, RECRUITER: recruiterNav, STAFF: staffNav };

const ROLE_COLOR = {
  STUDENT:   { grad: "from-teal-600 to-cyan-500",   badge: "bg-teal-500/20 text-teal-300",   accent: "teal" },
  RECRUITER: { grad: "from-sky-600 to-blue-500",    badge: "bg-sky-500/20 text-sky-300",     accent: "sky"  },
  STAFF:     { grad: "from-purple-600 to-violet-500", badge: "bg-purple-500/20 text-purple-300", accent: "purple" },
};

const TYPE_ICONS = {
  JOB_APPLICATION:    "💼",
  INTERVIEW_SCHEDULED:"📅",
  STATUS_UPDATE:      "📊",
};

function relativeTime(dateStr) {
  if (!dateStr) return "";
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) return "";
  const diff = Date.now() - date.getTime();
  const sec  = Math.floor(diff / 1000);
  if (sec < 60)  return "just now";
  const min  = Math.floor(sec / 60);
  if (min < 60)  return `${min}m ago`;
  const hr   = Math.floor(min / 60);
  if (hr < 24)   return `${hr}h ago`;
  return `${Math.floor(hr / 24)}d ago`;
}

// ─── Main AppShell ────────────────────────────────────────────────────────

export const AppShell = ({ role, children }) => {
  const { session, logout } = useAuth();
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount]     = useState(0);
  const [showNotif, setShowNotif]         = useState(false);
  const [sidebarOpen, setSidebarOpen]     = useState(false);
  const notifRef = useRef(null);

  const navItems  = NAV_BY_ROLE[role] || studentNav;
  const roleLabel = role === "STUDENT" ? "Student" : role === "STAFF" ? "Staff" : "Recruiter";
  const rc        = ROLE_COLOR[role] || ROLE_COLOR.STUDENT;
  const initials  = (session?.name || "S").split(" ").map(p => p[0]).join("").slice(0, 2).toUpperCase();

  const fetchNotifications = async () => {
    try {
      const [nr, cr] = await Promise.all([
        api.get("/api/notifications/"),
        api.get("/api/notifications/unread-count"),
      ]);
      setNotifications(nr.data || []);
      setUnreadCount(cr.data?.count ?? 0);
    } catch { /* non-critical */ }
  };

  useEffect(() => {
    fetchNotifications();
    const id = setInterval(fetchNotifications, 30000);
    return () => clearInterval(id);
  }, []);

  useEffect(() => {
    const handler = (e) => { if (notifRef.current && !notifRef.current.contains(e.target)) setShowNotif(false); };
    if (showNotif) document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, [showNotif]);

  const markRead = async (n) => {
    if (n.read) return;
    try {
      await api.patch(`/api/notifications/${n.id}/read`);
      setNotifications(prev => prev.map(x => x.id === n.id ? {...x, read: true} : x));
      setUnreadCount(prev => Math.max(0, prev - 1));
    } catch {}
  };

  const markAllRead = async () => {
    try {
      await api.post("/api/notifications/mark-all-read");
      setNotifications(prev => prev.map(n => ({...n, read: true})));
      setUnreadCount(0);
    } catch {}
  };

  const SidebarContent = () => (
    <>
      {/* Brand */}
      <div className="flex items-center gap-3 px-4 pt-5 pb-4" style={{animation:"slideInLeft 0.4s ease both"}}>
        <div className="portal-brand-icon text-base flex-shrink-0">S</div>
        <div>
          <p className="text-base font-black leading-none" style={{color:"#ffffff",fontFamily:"var(--font-display)"}}>Skillora</p>
          <span className={`mt-0.5 inline-block rounded-full px-2 py-0.5 text-[10px] font-bold uppercase tracking-wider ${rc.badge}`}>
            {roleLabel}
          </span>
        </div>
      </div>

      {/* User card */}
      <div className={`mx-3 mb-4 rounded-2xl bg-gradient-to-r ${rc.grad} p-3`} style={{animation:"slideInLeft 0.4s ease 0.05s both"}}>
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full text-sm font-bold" style={{background:"rgba(255,255,255,0.2)",color:"#ffffff"}}>
            {initials}
          </div>
          <div className="min-w-0">
            <p className="truncate text-sm font-bold" style={{color:"#ffffff"}}>{session?.name}</p>
            <p className="text-xs truncate" style={{color:"rgba(255,255,255,0.65)"}}>{session?.email}</p>
          </div>
        </div>
      </div>

      {/* Nav */}
      <nav className="flex-1 space-y-0.5 px-2 pb-4 overflow-y-auto">
        {navItems.map((item, i) => (
          <NavLink
            key={item.to}
            to={item.to}
            onClick={() => setSidebarOpen(false)}
            style={{animation:`slideInLeft 0.4s ease ${0.08 + i * 0.04}s both`}}
            className={({ isActive }) =>
              `group flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-all duration-150 ${
                isActive
                  ? `bg-gradient-to-r ${rc.grad} shadow-lg`
                  : "hover:bg-white/10"
              }`
            }
          >
            {({ isActive }) => (
              <>
                <span className="text-base w-5 text-center leading-none">{item.icon}</span>
                <span style={{color: isActive ? "#ffffff" : "#cbd5e1"}}>{item.label}</span>
              </>
            )}
          </NavLink>
        ))}
      </nav>

      {/* Logout */}
      <div className="px-3 pb-5" style={{animation:"slideInLeft 0.4s ease 0.35s both"}}>
        <button
          type="button"
          onClick={logout}
          className="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition hover:bg-rose-500/10"
          style={{border:"1px solid rgba(255,255,255,0.08)",color:"#94a3b8"}}
          onMouseEnter={e => { e.currentTarget.style.color = "#fca5a5"; e.currentTarget.style.borderColor = "rgba(239,68,68,0.3)"; }}
          onMouseLeave={e => { e.currentTarget.style.color = "#94a3b8"; e.currentTarget.style.borderColor = "rgba(255,255,255,0.08)"; }}
        >
          <span className="text-base w-5 text-center">🚪</span>
          Sign Out
        </button>
      </div>
    </>
  );

  return (
    <div className="min-h-screen flex bg-slate-50">
      {/* Desktop sidebar */}
      <aside className="hidden lg:flex w-64 shrink-0 flex-col bg-gradient-to-b from-slate-900 to-slate-800 border-r border-white/5">
        <SidebarContent />
      </aside>

      {/* Mobile sidebar overlay */}
      {sidebarOpen && (
        <div className="fixed inset-0 z-40 lg:hidden">
          <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={() => setSidebarOpen(false)} />
          <aside className="absolute left-0 top-0 bottom-0 w-64 flex flex-col bg-gradient-to-b from-slate-900 to-slate-800 z-50 shadow-2xl">
            <SidebarContent />
          </aside>
        </div>
      )}

      {/* Main content */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Top header */}
        <header className="sticky top-0 z-30 flex items-center justify-between gap-3 border-b border-slate-200 bg-white/95 backdrop-blur-md px-4 py-3 lg:px-6">
          {/* Mobile hamburger */}
          <button
            type="button"
            className="lg:hidden rounded-xl border border-slate-200 p-2 text-slate-600 hover:bg-slate-50"
            onClick={() => setSidebarOpen(true)}
          >
            <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>

          {/* Page breadcrumb area (desktop) */}
          <div className="hidden lg:flex items-center gap-2 text-sm text-slate-500">
            <span className="font-semibold text-slate-800">{roleLabel} Portal</span>
          </div>

          <div className="flex items-center gap-2 ml-auto">
            {/* Notification bell */}
            <div className="relative" ref={notifRef}>
              <button
                type="button"
                aria-label="Notifications"
                onClick={() => setShowNotif(v => !v)}
                className="relative flex h-9 w-9 items-center justify-center rounded-xl border border-slate-200 bg-white text-slate-600 transition hover:bg-slate-50 hover:border-slate-300"
              >
                <svg viewBox="0 0 24 24" fill="none" className="h-4.5 w-4.5" stroke="currentColor" strokeWidth="1.8">
                  <path d="M15 18H5.8a1 1 0 0 1-.8-1.6l1.3-1.7v-4.2a5.7 5.7 0 1 1 11.4 0v4.2l1.3 1.7a1 1 0 0 1-.8 1.6H15z" />
                  <path d="M10.2 18a1.8 1.8 0 0 0 3.6 0" />
                </svg>
                {unreadCount > 0 && (
                  <span className="absolute -right-1 -top-1 flex h-4 min-w-4 items-center justify-center rounded-full bg-red-500 px-1 text-[9px] font-bold text-white animate-pulse">
                    {unreadCount > 9 ? "9+" : unreadCount}
                  </span>
                )}
              </button>

              {showNotif && (
                <div className="absolute right-0 z-50 mt-2 w-80 rounded-2xl border border-slate-200 bg-white shadow-2xl shadow-slate-900/10 overflow-hidden">
                  <div className="flex items-center justify-between border-b border-slate-100 bg-slate-50 px-4 py-3">
                    <div className="flex items-center gap-2">
                      <span className="text-base">🔔</span>
                      <span className="text-sm font-bold text-slate-900">Notifications</span>
                      {unreadCount > 0 && (
                        <span className="rounded-full bg-red-500 px-1.5 py-0.5 text-[10px] font-bold text-white">{unreadCount}</span>
                      )}
                    </div>
                    {unreadCount > 0 && (
                      <button type="button" onClick={markAllRead}
                        className="text-xs font-semibold text-teal-600 hover:text-teal-800">
                        Mark all read
                      </button>
                    )}
                  </div>
                  <div className="max-h-72 overflow-y-auto divide-y divide-slate-50">
                    {notifications.slice(0, 10).length === 0 ? (
                      <div className="py-8 text-center">
                        <p className="text-2xl mb-2">📭</p>
                        <p className="text-sm text-slate-400">No notifications yet</p>
                      </div>
                    ) : notifications.slice(0, 10).map(n => (
                      <button key={n.id} type="button" onClick={() => markRead(n)}
                        className={`w-full px-4 py-3 text-left transition hover:bg-slate-50 ${!n.read ? "bg-teal-50/40" : ""}`}>
                        <div className="flex items-start gap-2.5">
                          <span className="mt-0.5 text-base shrink-0">{TYPE_ICONS[n.type] ?? "🔔"}</span>
                          <div className="flex-1 min-w-0">
                            <p className={`text-xs leading-snug text-slate-800 ${!n.read ? "font-semibold" : ""}`}>{n.message}</p>
                            <p className="mt-1 text-[10px] text-slate-400">{relativeTime(n.createdAt)}</p>
                          </div>
                          {!n.read && <span className="mt-1 h-2 w-2 shrink-0 rounded-full bg-teal-500" />}
                        </div>
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </div>

            {/* User avatar */}
            <button
              type="button"
              className={`flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br ${rc.grad} text-xs font-bold text-white shadow-lg transition hover:scale-105`}
              title={session?.name}
            >
              {initials}
            </button>
          </div>
        </header>

        {/* Page content */}
        <main className="flex-1 p-4 lg:p-6 xl:p-8">
          {children}
        </main>
      </div>
    </div>
  );
};

// ─── KPI / Stat Card ──────────────────────────────────────────────────────

export const StatCard = ({ label, value, helper, icon, trend, color = "teal", onClick }) => {
  const colorMap = {
    teal:   { bg: "bg-teal-50",   ring: "ring-teal-200",   icon: "bg-teal-500",  text: "text-teal-700"  },
    sky:    { bg: "bg-sky-50",    ring: "ring-sky-200",    icon: "bg-sky-500",   text: "text-sky-700"   },
    purple: { bg: "bg-purple-50", ring: "ring-purple-200", icon: "bg-purple-500",text: "text-purple-700"},
    amber:  { bg: "bg-amber-50",  ring: "ring-amber-200",  icon: "bg-amber-500", text: "text-amber-700" },
    rose:   { bg: "bg-rose-50",   ring: "ring-rose-200",   icon: "bg-rose-500",  text: "text-rose-700"  },
    emerald:{ bg: "bg-emerald-50",ring: "ring-emerald-200",icon: "bg-emerald-500",text:"text-emerald-700"},
    slate:  { bg: "bg-slate-50",  ring: "ring-slate-200",  icon: "bg-slate-500", text: "text-slate-700" },
  };
  const c = colorMap[color] || colorMap.teal;
  const trendUp   = trend > 0;
  const trendDown = trend < 0;

  return (
    <div
      onClick={onClick}
      role={onClick ? "button" : undefined}
      className={`group relative overflow-hidden rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition-all duration-200 
        hover:shadow-md hover:-translate-y-0.5 hover:border-slate-300
        ${onClick ? "cursor-pointer" : ""}
      `}
    >
      {/* Accent stripe */}
      <div className={`absolute top-0 left-0 right-0 h-0.5 bg-gradient-to-r from-${color}-400 to-${color}-600 opacity-0 group-hover:opacity-100 transition-opacity`} />

      <div className="flex items-start justify-between">
        <div className="flex-1 min-w-0">
          <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">{label}</p>
          <p className="mt-1.5 text-2xl font-black text-slate-900 leading-none tracking-tight">{value}</p>
          {helper && <p className="mt-1.5 text-xs text-slate-400">{helper}</p>}
          {trend !== undefined && (
            <div className={`mt-2 flex items-center gap-1 text-xs font-semibold ${trendUp ? "text-emerald-600" : trendDown ? "text-rose-500" : "text-slate-400"}`}>
              <span>{trendUp ? "↑" : trendDown ? "↓" : "→"}</span>
              <span>{Math.abs(trend)}% vs last week</span>
            </div>
          )}
        </div>
        {icon && (
          <div className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-xl ${c.icon} shadow-lg text-white text-lg ml-3`}>
            {icon}
          </div>
        )}
      </div>

      {/* Subtle background tint on hover */}
      <div className={`absolute inset-0 ${c.bg} opacity-0 group-hover:opacity-30 transition-opacity pointer-events-none`} />
    </div>
  );
};

// ─── Progress Bar ─────────────────────────────────────────────────────────

export const ProgressBar = ({ label, value, color = "teal", showPercent = true }) => {
  const pct = Math.max(0, Math.min(100, value || 0));
  const gradMap = {
    teal:   "from-teal-500 to-emerald-400",
    sky:    "from-sky-500 to-blue-400",
    purple: "from-purple-500 to-violet-400",
    amber:  "from-amber-500 to-orange-400",
    rose:   "from-rose-500 to-pink-400",
  };
  const grad = gradMap[color] || gradMap.teal;

  return (
    <div className="group">
      <div className="mb-1.5 flex items-center justify-between text-sm">
        <span className="font-medium text-slate-700">{label}</span>
        {showPercent && (
          <span className="font-bold text-slate-900 tabular-nums">{Math.round(pct)}%</span>
        )}
      </div>
      <div className="relative h-2.5 w-full overflow-hidden rounded-full bg-slate-100">
        <div
          className={`h-full rounded-full bg-gradient-to-r ${grad} transition-all duration-700 ease-out`}
          style={{ width: `${pct}%` }}
        />
        {/* Shimmer effect */}
        <div
          className="absolute inset-y-0 left-0 bg-white/30 w-1/3 -skew-x-12 opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none"
          style={{ transform: `translateX(${pct}%) skewX(-20deg)` }}
        />
      </div>
    </div>
  );
};

// ─── Toggle Button Group ──────────────────────────────────────────────────

export const ToggleGroup = ({ options, value, onChange, color = "teal" }) => {
  const activeMap = {
    teal:   "bg-gradient-to-r from-teal-600 to-emerald-500 text-white shadow-lg shadow-teal-500/30",
    sky:    "bg-gradient-to-r from-sky-600 to-blue-500 text-white shadow-lg shadow-sky-500/30",
    purple: "bg-gradient-to-r from-purple-600 to-violet-500 text-white shadow-lg shadow-purple-500/30",
    amber:  "bg-gradient-to-r from-amber-500 to-orange-400 text-white shadow-lg shadow-amber-500/30",
    rose:   "bg-gradient-to-r from-rose-600 to-pink-500 text-white shadow-lg shadow-rose-500/30",
  };
  const active = activeMap[color] || activeMap.teal;

  return (
    <div className="inline-flex items-center gap-1 rounded-2xl border border-slate-200 bg-slate-100 p-1">
      {options.map(opt => (
        <button
          key={opt.value ?? opt}
          type="button"
          onClick={() => onChange(opt.value ?? opt)}
          className={`rounded-xl px-4 py-1.5 text-sm font-semibold transition-all duration-200 ${
            (opt.value ?? opt) === value
              ? active
              : "text-slate-600 hover:text-slate-900 hover:bg-white/60"
          }`}
        >
          {opt.label ?? opt}
        </button>
      ))}
    </div>
  );
};

// ─── Badge ────────────────────────────────────────────────────────────────

export const Badge = ({ children, color = "teal", size = "sm" }) => {
  const colorMap = {
    teal:    "bg-teal-50 text-teal-700 border-teal-200",
    sky:     "bg-sky-50 text-sky-700 border-sky-200",
    purple:  "bg-purple-50 text-purple-700 border-purple-200",
    amber:   "bg-amber-50 text-amber-700 border-amber-200",
    rose:    "bg-rose-50 text-rose-700 border-rose-200",
    emerald: "bg-emerald-50 text-emerald-700 border-emerald-200",
    slate:   "bg-slate-100 text-slate-600 border-slate-200",
  };
  return (
    <span className={`inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold ${colorMap[color] || colorMap.teal}`}>
      {children}
    </span>
  );
};

// ─── Empty State ──────────────────────────────────────────────────────────

export const EmptyState = ({ icon = "📭", title, description, action, actionLabel }) => (
  <div className="flex flex-col items-center justify-center py-16 text-center">
    <div className="mb-4 text-5xl">{icon}</div>
    <h3 className="text-base font-bold text-slate-800">{title}</h3>
    {description && <p className="mt-1 max-w-xs text-sm text-slate-500">{description}</p>}
    {action && (
      <button type="button" onClick={action}
        className="mt-4 rounded-xl bg-teal-700 px-5 py-2 text-sm font-semibold text-white hover:bg-teal-600 transition">
        {actionLabel || "Get Started"}
      </button>
    )}
  </div>
);

// ─── Loading Skeleton ─────────────────────────────────────────────────────

export const SkeletonCard = ({ lines = 3 }) => (
  <div className="dashboard-card p-5 animate-pulse">
    <div className="h-3 w-24 rounded bg-slate-200 mb-3" />
    <div className="h-7 w-16 rounded bg-slate-200 mb-4" />
    {[...Array(lines - 1)].map((_, i) => (
      <div key={i} className={`h-2 rounded bg-slate-100 mb-2 ${i % 2 === 0 ? "w-full" : "w-3/4"}`} />
    ))}
  </div>
);
