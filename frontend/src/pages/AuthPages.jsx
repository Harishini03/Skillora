import { useEffect, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../context/useAuth";
import { signInWithEmailAndPassword, signInWithPopup } from "firebase/auth";
import { firebaseAuth, googleProvider, isFirebaseAuthConfigured } from "../lib/firebase";

const roleOptions = ["STUDENT", "STAFF", "RECRUITER"];
const LOGIN_DRAFT_KEY  = "skillora_login_draft_v1";
const SIGNUP_DRAFT_KEY = "skillora_signup_draft_v1";
const DEMO_PASSWORD    = "Skillora@123";
const DEMO_USERS = { STUDENT: "student.demo", STAFF: "staff.demo", RECRUITER: "recruiter.demo" };

const readDraft = (key, fallback) => {
  try { const raw = localStorage.getItem(key); return raw ? { ...fallback, ...JSON.parse(raw) } : fallback; }
  catch { return fallback; }
};

const homeByRole = (role) => {
  if (role === "STUDENT") return "/student/dashboard";
  if (role === "STAFF")   return "/staff/dashboard";
  return "/recruiter/dashboard";
};

const normalizeRole = (rawRole) => (roleOptions.includes(rawRole) ? rawRole : "STUDENT");

// ─── Landing Page ──────────────────────────────────────────────────────────

export const LandingPage = () => (
  <div className="min-h-screen bg-gradient-to-br from-slate-950 via-teal-950 to-slate-900">
    {/* Navbar */}
    <nav className="mx-auto flex max-w-7xl items-center justify-between px-6 py-5">
      <div className="flex items-center gap-3">
        <div className="portal-brand-icon text-lg">S</div>
        <span className="text-xl font-black text-white tracking-tight" style={{fontFamily:"var(--font-display)"}}>Skillora</span>
      </div>
      <div className="flex items-center gap-3">
        <Link to="/login" className="rounded-xl border border-white/20 px-4 py-2 text-sm font-semibold text-white/90 hover:bg-white/10 transition">
          Sign In
        </Link>
        <Link to="/signup" className="rounded-xl bg-teal-500 px-4 py-2 text-sm font-bold text-white hover:bg-teal-400 transition shadow-lg shadow-teal-500/25">
          Get Started →
        </Link>
      </div>
    </nav>

    {/* Hero */}
    <section className="mx-auto max-w-5xl px-6 pt-16 pb-20 text-center">
      <div className="inline-flex items-center gap-2 rounded-full border border-teal-500/30 bg-teal-500/10 px-4 py-1.5 text-xs font-semibold text-teal-300 mb-6">
        ✨ AI-Powered Placement Intelligence
      </div>
      <h1 className="text-5xl font-black leading-tight text-white md:text-6xl lg:text-7xl" style={{fontFamily:"var(--font-display)",letterSpacing:"-0.03em"}}>
        Ace Your
        <span className="block bg-gradient-to-r from-teal-400 to-emerald-300 bg-clip-text text-transparent">
          Placement
        </span>
        With AI
      </h1>
      <p className="mx-auto mt-6 max-w-2xl text-lg text-slate-300">
        Skillora combines AI-powered learning, real aptitude tests, DSA practice, and smart analytics to get you job-ready faster.
      </p>
      <div className="mt-8 flex flex-wrap items-center justify-center gap-4">
        <Link to="/signup?role=STUDENT" className="rounded-2xl bg-gradient-to-r from-teal-500 to-emerald-400 px-8 py-3.5 text-base font-bold text-white shadow-xl shadow-teal-500/30 hover:opacity-90 transition">
          Start Preparing Free →
        </Link>
        <Link to="/login" className="rounded-2xl border border-white/20 bg-white/5 px-8 py-3.5 text-base font-semibold text-white hover:bg-white/10 transition">
          Sign In
        </Link>
      </div>

      {/* Stats strip */}
      <div className="mx-auto mt-14 grid max-w-2xl grid-cols-4 gap-4 border border-white/10 rounded-2xl bg-white/5 p-5 backdrop-blur">
        {[["4500+","Students Trained"],["300+","Partner Companies"],["95%","Placement Rate"],["AI","Powered Engine"]].map(([val, lab]) => (
          <div key={lab} className="text-center">
            <p className="text-2xl font-black text-teal-400">{val}</p>
            <p className="mt-0.5 text-xs text-slate-400">{lab}</p>
          </div>
        ))}
      </div>
    </section>

    {/* Feature cards */}
    <section className="mx-auto max-w-6xl px-6 pb-20">
      <h2 className="mb-8 text-center text-2xl font-bold text-white" style={{fontFamily:"var(--font-display)"}}>
        Everything you need to get placed
      </h2>
      <div className="grid gap-4 md:grid-cols-3">
        {[
          { icon:"🎓", title:"For Students", role:"STUDENT", color:"from-teal-500/20 to-emerald-500/10", border:"border-teal-500/20",
            features:["AI-powered learning modules","Aptitude & DSA practice","Mock tests with analytics","Readiness score tracking"] },
          { icon:"💼", title:"For Recruiters", role:"RECRUITER", color:"from-sky-500/20 to-blue-500/10", border:"border-sky-500/20",
            features:["Verified candidate profiles","Smart candidate search","Interview scheduling","Placement analytics"] },
          { icon:"📊", title:"For Staff/Admin", role:"STAFF", color:"from-purple-500/20 to-violet-500/10", border:"border-purple-500/20",
            features:["Department-wise analytics","Student cohort tracking","Placement statistics","Custom reports & exports"] },
        ].map(c => (
          <div key={c.title} className={`rounded-2xl border ${c.border} bg-gradient-to-br ${c.color} p-6 backdrop-blur hover:scale-[1.02] transition`}>
            <div className="mb-3 text-4xl">{c.icon}</div>
            <h3 className="mb-3 text-lg font-bold text-white">{c.title}</h3>
            <ul className="space-y-2">
              {c.features.map(f => (
                <li key={f} className="flex items-center gap-2 text-sm text-slate-300">
                  <span className="text-teal-400">✓</span> {f}
                </li>
              ))}
            </ul>
            <Link to={`/login?role=${c.role}`}
              className="mt-5 inline-block rounded-xl border border-white/20 px-4 py-2 text-xs font-semibold text-white hover:bg-white/10 transition">
              Enter Portal →
            </Link>
          </div>
        ))}
      </div>
    </section>

    <footer className="border-t border-white/10 py-6 text-center text-xs text-slate-600">
      © 2025 Skillora · AI-Powered Placement Intelligence
    </footer>
  </div>
);

// ─── Login Page ────────────────────────────────────────────────────────────

const ROLE_META = {
  STUDENT:   { icon: "🎓", label: "Student",  desc: "Practice & get placed" },
  STAFF:     { icon: "📊", label: "Staff",     desc: "Monitor & manage" },
  RECRUITER: { icon: "💼", label: "Recruiter", desc: "Hire top talent" },
};

export const LoginPage = () => {
  const navigate = useNavigate();
  const { login, googleLogin, firebaseLogin } = useAuth();
  const [params] = useSearchParams();
  const roleFromQuery = normalizeRole(params.get("role") || "STUDENT");
  const [form, setForm] = useState(() => {
    const draft = readDraft(LOGIN_DRAFT_KEY, { usernameOrEmail: "", role: roleFromQuery });
    return { usernameOrEmail: draft.usernameOrEmail || "", password: "", role: normalizeRole(draft.role || roleFromQuery) };
  });
  const [error, setError]     = useState("");
  const [loading, setLoading] = useState(false);
  const [showPwd, setShowPwd] = useState(false);

  const applyDemo = (role) => {
    setForm(f => ({ ...f, role, usernameOrEmail: DEMO_USERS[role], password: DEMO_PASSWORD }));
    setError("");
  };

  useEffect(() => {
    localStorage.setItem(LOGIN_DRAFT_KEY, JSON.stringify({ usernameOrEmail: form.usernameOrEmail, role: form.role }));
  }, [form.usernameOrEmail, form.role]);

  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");
    if (!form.usernameOrEmail || !form.password) { setError("Email/username and password are required"); return; }
    setLoading(true);
    try {
      const data = await login(form);
      localStorage.removeItem(LOGIN_DRAFT_KEY);
      navigate(homeByRole(data.portalRole || form.role));
    } catch (err) {
      const msg = err.response?.data?.message || "Login failed";
      if (msg.includes("Role mismatch")) {
        try {
          const data = await login({ ...form, role: undefined });
          localStorage.removeItem(LOGIN_DRAFT_KEY);
          navigate(homeByRole(data.portalRole || data.role || form.role));
          return;
        } catch (fe) { setError(fe.response?.data?.message || "Login failed"); return; }
      }
      setError(msg);
    } finally { setLoading(false); }
  };

  const handleGoogle = async () => {
    try {
      const email = window.prompt("Google email"); if (!email) return;
      const name  = window.prompt("Display name") || "Google User";
      const dept  = window.prompt("Department (first-time signup)", "Computer Science") || "";
      const data  = await googleLogin({ email, name, role: form.role, departmentName: dept });
      navigate(homeByRole(data.portalRole || form.role));
    } catch (e) { setError(e.response?.data?.message || "Google sign-in failed"); }
  };

  const handleFirebaseEmail = async () => {
    setError("");
    if (!form.usernameOrEmail || !form.password) { setError("Email and password required"); return; }
    try {
      const result = await signInWithEmailAndPassword(firebaseAuth, form.usernameOrEmail, form.password);
      const data   = await firebaseLogin({ firebaseUser: result.user, role: form.role });
      localStorage.removeItem(LOGIN_DRAFT_KEY);
      navigate(homeByRole(data.portalRole || form.role));
    } catch (e) { setError(e.message || "Firebase sign-in failed"); }
  };

  const handleFirebaseGoogle = async () => {
    setError("");
    try {
      const result = await signInWithPopup(firebaseAuth, googleProvider);
      const data   = await firebaseLogin({ firebaseUser: result.user, role: form.role, departmentName: "" });
      localStorage.removeItem(LOGIN_DRAFT_KEY);
      navigate(homeByRole(data.portalRole || form.role));
    } catch (e) { setError(e.message || "Firebase Google sign-in failed"); }
  };

  return (
    <div className="min-h-screen grid lg:grid-cols-2">
      {/* ── Left Branding Panel ── */}
      <div className="hidden lg:flex flex-col justify-between relative overflow-hidden p-12"
        style={{background:"linear-gradient(145deg,#020d0b 0%,#041f1a 40%,#0a3d35 70%,#0f5144 100%)"}}>

        {/* Animated background orbs */}
        <div className="absolute inset-0 overflow-hidden pointer-events-none">
          <div style={{
            position:"absolute", top:"10%", right:"5%",
            width:280, height:280, borderRadius:"50%",
            background:"radial-gradient(circle,rgba(20,184,166,0.18) 0%,transparent 70%)",
            animation:"pulse 4s ease-in-out infinite"
          }}/>
          <div style={{
            position:"absolute", bottom:"15%", left:"-5%",
            width:220, height:220, borderRadius:"50%",
            background:"radial-gradient(circle,rgba(5,150,105,0.15) 0%,transparent 70%)",
            animation:"pulse 5s ease-in-out infinite 1s"
          }}/>
          <div style={{
            position:"absolute", top:"50%", left:"30%",
            width:160, height:160, borderRadius:"50%",
            background:"radial-gradient(circle,rgba(245,158,11,0.08) 0%,transparent 70%)",
            animation:"pulse 6s ease-in-out infinite 2s"
          }}/>
          {/* Grid lines */}
          <div style={{
            position:"absolute", inset:0,
            backgroundImage:"linear-gradient(rgba(255,255,255,0.03) 1px,transparent 1px),linear-gradient(90deg,rgba(255,255,255,0.03) 1px,transparent 1px)",
            backgroundSize:"40px 40px"
          }}/>
        </div>

        {/* Logo */}
        <div className="relative flex items-center gap-3" style={{animation:"slideInLeft 0.6s ease both"}}>
          <div className="portal-brand-icon text-lg flex-shrink-0">S</div>
          <span className="text-2xl font-black" style={{color:"#ffffff",fontFamily:"var(--font-display)"}}>Skillora</span>
        </div>

        {/* Main content */}
        <div className="relative" style={{animation:"slideInLeft 0.7s ease 0.1s both"}}>
          {/* Badge */}
          <div className="inline-flex items-center gap-2 rounded-full border px-3 py-1.5 mb-6" style={{borderColor:"rgba(20,184,166,0.4)",background:"rgba(20,184,166,0.1)"}}>
            <span className="h-2 w-2 rounded-full animate-pulse" style={{background:"#14b8a6"}}/>
            <span className="text-xs font-semibold" style={{color:"#5eead4"}}>AI-Powered Platform</span>
          </div>

          <h2 className="text-5xl font-black leading-tight" style={{fontFamily:"var(--font-display)",letterSpacing:"-0.03em",color:"#ffffff"}}>
            Your placement<br/>
            <span style={{
              background:"linear-gradient(90deg,#34d399,#14b8a6,#06b6d4)",
              WebkitBackgroundClip:"text",
              WebkitTextFillColor:"transparent",
              backgroundClip:"text"
            }}>starts here</span>
          </h2>

          <p className="mt-4 text-base leading-relaxed" style={{color:"#94a3b8"}}>
            AI-powered preparation for campus and off-campus placements.
          </p>

          {/* Feature list */}
          <ul className="mt-8 space-y-4">
            {[
              {icon:"🧠", text:"AI aptitude & DSA question generation"},
              {icon:"📈", text:"Adaptive learning with performance tracking"},
              {icon:"🏢", text:"Company-specific mock tests (TCS, Infosys, Amazon)"},
              {icon:"📊", text:"Real-time placement analytics & readiness score"},
            ].map((f, i) => (
              <li key={f.text}
                className="flex items-center gap-3"
                style={{animation:`slideInLeft 0.5s ease ${0.2 + i * 0.1}s both`}}>
                <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-xl text-base"
                  style={{background:"rgba(20,184,166,0.15)",border:"1px solid rgba(20,184,166,0.3)"}}>
                  {f.icon}
                </div>
                <span className="text-sm font-medium" style={{color:"#e2e8f0"}}>{f.text}</span>
              </li>
            ))}
          </ul>

          {/* Stats strip */}
          <div className="mt-10 grid grid-cols-3 gap-3">
            {[["4500+","Students"],["95%","Placement Rate"],["50+","Companies"]].map(([val, lab]) => (
              <div key={lab} className="rounded-2xl p-3 text-center"
                style={{background:"rgba(255,255,255,0.05)",border:"1px solid rgba(255,255,255,0.08)"}}>
                <p className="text-xl font-black" style={{color:"#34d399",fontFamily:"var(--font-display)"}}>{val}</p>
                <p className="text-xs mt-0.5" style={{color:"#64748b"}}>{lab}</p>
              </div>
            ))}
          </div>
        </div>

        <p className="relative text-xs" style={{color:"#334155",animation:"fadeIn 1s ease 0.8s both"}}>
          © 2025 Skillora · AI-Powered Placement Intelligence
        </p>
      </div>

      {/* Right form */}
      <div className="flex items-center justify-center bg-white p-6 lg:p-12">
        <div className="w-full max-w-md">
          <div className="mb-8 lg:hidden flex items-center gap-2">
            <div className="portal-brand-icon text-sm">S</div>
            <span className="text-lg font-black text-slate-900" style={{fontFamily:"var(--font-display)"}}>Skillora</span>
          </div>
          <h1 className="text-3xl font-black text-slate-900" style={{fontFamily:"var(--font-display)"}}>Welcome back</h1>
          <p className="mt-1 text-sm text-slate-500">Sign in to your Skillora portal</p>

          {/* Role pills */}
          <div className="mt-6 grid grid-cols-3 gap-2">
            {roleOptions.map(role => {
              const m = ROLE_META[role];
              return (
                <button key={role} type="button" onClick={() => setForm(f => ({...f, role: normalizeRole(role)}))}
                  className={`rounded-xl border-2 p-3 text-center transition ${form.role === role ? "border-teal-500 bg-teal-50" : "border-slate-200 hover:border-slate-300"}`}>
                  <div className="text-xl">{m.icon}</div>
                  <p className="mt-1 text-xs font-bold text-slate-900">{m.label}</p>
                  <p className="text-[10px] text-slate-500">{m.desc}</p>
                </button>
              );
            })}
          </div>

          <form onSubmit={handleLogin} className="mt-5 space-y-3">
            <input
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm focus:border-teal-500 focus:outline-none focus:ring-2 focus:ring-teal-500/20 transition"
              placeholder="Email or username"
              value={form.usernameOrEmail}
              onChange={e => setForm(f => ({...f, usernameOrEmail: e.target.value}))}
              autoComplete="username"
              style={{color:"#0f172a"}}
            />
            <div className="relative">
              <input
                type={showPwd ? "text" : "password"}
                className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 pr-10 text-sm focus:border-teal-500 focus:outline-none focus:ring-2 focus:ring-teal-500/20 transition"
                placeholder="Password"
                value={form.password}
                onChange={e => setForm(f => ({...f, password: e.target.value}))}
                autoComplete="current-password"
                style={{color:"#0f172a"}}
              />
              <button type="button" onClick={() => setShowPwd(v => !v)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition"
                tabIndex={-1} aria-label={showPwd ? "Hide password" : "Show password"}>
                {showPwd ? "🙈" : "👁️"}
              </button>
            </div>
            {error && <div className="rounded-xl border border-rose-200 bg-rose-50 px-3 py-2 text-xs text-rose-700">{error}</div>}
            <button type="submit" disabled={loading}
              className="w-full rounded-xl bg-gradient-to-r from-teal-600 to-emerald-500 py-2.5 font-bold text-white shadow-lg shadow-teal-500/25 hover:opacity-90 transition disabled:opacity-60">
              {loading ? "Signing in..." : `Sign in as ${ROLE_META[form.role]?.label}`}
            </button>
          </form>

          {/* Demo */}
          <details className="mt-4">
            <summary className="cursor-pointer text-xs font-semibold text-slate-500 hover:text-slate-700">🎭 Use demo credentials</summary>
            <div className="mt-2 flex gap-2">
              {roleOptions.map(role => (
                <button key={role} type="button" onClick={() => applyDemo(role)}
                  className="flex-1 rounded-lg border border-slate-200 py-1.5 text-xs font-semibold text-slate-600 hover:bg-slate-50 transition">
                  {ROLE_META[role].icon} {ROLE_META[role].label}
                </button>
              ))}
            </div>
            <p className="mt-1 text-[10px] text-slate-400">Password: {DEMO_PASSWORD}</p>
          </details>

          <div className="my-4 flex items-center gap-3">
            <div className="h-px flex-1 bg-slate-200" />
            <span className="text-xs text-slate-400">or</span>
            <div className="h-px flex-1 bg-slate-200" />
          </div>

          <button type="button" onClick={handleGoogle}
            className="w-full rounded-xl border border-slate-200 py-2.5 text-sm font-semibold text-slate-700 hover:bg-slate-50 transition">
            🌐 Sign in with Google
          </button>

          {isFirebaseAuthConfigured() && (
            <div className="mt-2 grid grid-cols-2 gap-2">
              <button type="button" onClick={handleFirebaseEmail}
                className="rounded-xl border border-blue-200 bg-blue-50 py-2 text-xs font-semibold text-blue-700 hover:bg-blue-100 transition">
                🔥 Firebase Email
              </button>
              <button type="button" onClick={handleFirebaseGoogle}
                className="rounded-xl border border-orange-200 bg-orange-50 py-2 text-xs font-semibold text-orange-700 hover:bg-orange-100 transition">
                🔥 Firebase Google
              </button>
            </div>
          )}

          <p className="mt-6 text-center text-sm text-slate-500">
            New to Skillora? <Link to="/signup" className="font-semibold text-teal-700 hover:text-teal-600">Create account</Link>
          </p>
        </div>
      </div>
    </div>
  );
};

// ─── Signup Page ───────────────────────────────────────────────────────────

export const SignupPage = () => {
  const navigate = useNavigate();
  const { signup, firebaseLogin } = useAuth();
  const signupDraft = readDraft(SIGNUP_DRAFT_KEY, {
    role: "STUDENT", name: "", email: "", username: "", departmentName: "",
    cgpa: 8, level: "Beginner", interests: "", skillIds: [1, 2],
  });
  const [role, setRole]   = useState(normalizeRole(signupDraft.role));
  const [error, setError] = useState("");
  const [showSignupPwd, setShowSignupPwd] = useState(false);
  const [form, setForm]   = useState({
    name: signupDraft.name || "", email: signupDraft.email || "",
    username: signupDraft.username || "", password: "",
    departmentName: signupDraft.departmentName || "", cgpa: signupDraft.cgpa ?? 8,
    level: signupDraft.level || "Beginner", interests: signupDraft.interests || "",
    skillIds: Array.isArray(signupDraft.skillIds) ? signupDraft.skillIds : [1, 2],
  });

  useEffect(() => {
    localStorage.setItem(SIGNUP_DRAFT_KEY, JSON.stringify({
      role, name: form.name, email: form.email, username: form.username,
      departmentName: form.departmentName, cgpa: form.cgpa, level: form.level,
      interests: form.interests, skillIds: form.skillIds,
    }));
  }, [role, form.name, form.email, form.username, form.departmentName, form.cgpa, form.level, form.interests, form.skillIds]);

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    if (!form.name || !form.email || !form.username || !form.password) { setError("Please fill all required fields"); return; }
    if (form.password.length < 8 || !/[A-Z]/.test(form.password) || !/[a-z]/.test(form.password)
        || !/\d/.test(form.password) || !/[^A-Za-z0-9]/.test(form.password)) {
      setError("Password must be 8+ chars with uppercase, lowercase, number and special character.");
      return;
    }
    if (role === "STUDENT" && !form.departmentName.trim()) { setError("Please enter your department name"); return; }
    try {
      const data = await signup({ ...form, role, skillIds: role === "STUDENT" ? form.skillIds : [], cgpa: role === "STUDENT" ? Number(form.cgpa) : 0 });
      localStorage.removeItem(SIGNUP_DRAFT_KEY);
      navigate(homeByRole(data.portalRole || role));
    } catch (e) { setError(e.response?.data?.message || "Signup failed"); }
  };

  const handleFirebaseGoogleSignup = async () => {
    setError("");
    try {
      const result = await signInWithPopup(firebaseAuth, googleProvider);
      const data   = await firebaseLogin({
        firebaseUser: result.user, role,
        departmentName: form.departmentName,
        cgpa: role === "STUDENT" ? Number(form.cgpa) : undefined,
        level: role === "STUDENT" ? form.level : undefined,
        interests: role === "STUDENT" ? form.interests : undefined,
      });
      localStorage.removeItem(SIGNUP_DRAFT_KEY);
      navigate(homeByRole(data.portalRole || role));
    } catch (e) { setError(e.response?.data?.message || e.message || "Firebase Google sign-up failed"); }
  };

  return (
    <div className="grid min-h-screen place-items-center bg-slate-50 p-6">
      <div className="w-full max-w-2xl rounded-3xl border border-slate-200 bg-white p-8 shadow-xl">
        <div className="flex items-center gap-3 mb-6">
          <div className="portal-brand-icon text-sm">S</div>
          <span className="text-xl font-black text-slate-900" style={{fontFamily:"var(--font-display)"}}>Skillora</span>
        </div>
        <h1 className="text-2xl font-black text-slate-900" style={{fontFamily:"var(--font-display)"}}>Create your account</h1>
        <p className="mt-1 text-sm text-slate-500">Choose your role and complete your profile.</p>

        {/* Role cards */}
        <div className="mt-5 grid grid-cols-3 gap-3">
          {roleOptions.map(r => {
            const m = ROLE_META[r];
            return (
              <button key={r} type="button" onClick={() => setRole(normalizeRole(r))}
                className={`rounded-xl border-2 p-3 text-center transition ${role === r ? "border-teal-500 bg-teal-50" : "border-slate-200 hover:border-slate-300"}`}>
                <div className="text-2xl">{m.icon}</div>
                <p className="mt-1 text-xs font-bold text-slate-900">{m.label}</p>
              </button>
            );
          })}
        </div>

        <form onSubmit={submit} className="mt-5 grid gap-3 md:grid-cols-2">
          {[
            { label:"Full Name", key:"name", placeholder:"Your full name" },
            { label:"Email", key:"email", placeholder:"your@email.com", type:"email" },
            { label:"Username", key:"username", placeholder:"Choose a username" },
            { label:"Department", key:"departmentName", placeholder:"e.g. Computer Science" },
          ].map(f => (
            <label key={f.key} className="text-sm font-semibold text-slate-700">
              {f.label}
              <input
                type={f.type || "text"}
                className="mt-1 w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm focus:border-teal-500 focus:outline-none"
                placeholder={f.placeholder}
                value={form[f.key]}
                onChange={e => setForm(prev => ({...prev, [f.key]: e.target.value}))}
              />
            </label>
          ))}
          <label className="text-sm font-semibold text-slate-700">
            Password
            <div className="relative mt-1">
              <input
                type={showSignupPwd ? "text" : "password"}
                className="w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 pr-10 text-sm focus:border-teal-500 focus:outline-none"
                placeholder="Strong password"
                value={form.password}
                onChange={e => setForm(prev => ({...prev, password: e.target.value}))}
                autoComplete="new-password"
              />
              <button type="button" onClick={() => setShowSignupPwd(v => !v)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition"
                tabIndex={-1} aria-label={showSignupPwd ? "Hide password" : "Show password"}>
                {showSignupPwd ? "🙈" : "👁️"}
              </button>
            </div>
          </label>

          {role === "STUDENT" && <>
            <label className="text-sm font-semibold text-slate-700">
              CGPA <span className="font-normal text-slate-400">(0-10)</span>
              <input type="number" step="0.01" min="0" max="10"
                className="mt-1 w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm focus:border-teal-500 focus:outline-none"
                value={form.cgpa} onChange={e => setForm(f => ({...f, cgpa: e.target.value}))} />
            </label>
            <label className="text-sm font-semibold text-slate-700">
              Level
              <select className="mt-1 w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm focus:border-teal-500 focus:outline-none"
                value={form.level} onChange={e => setForm(f => ({...f, level: e.target.value}))}>
                <option>Beginner</option><option>Intermediate</option><option>Advanced</option>
              </select>
            </label>
            <label className="text-sm font-semibold text-slate-700 md:col-span-2">
              Interests
              <input className="mt-1 w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm focus:border-teal-500 focus:outline-none"
                placeholder="e.g. Java, DSA, DBMS" value={form.interests}
                onChange={e => setForm(f => ({...f, interests: e.target.value}))} />
            </label>
          </>}

          {error && <p className="text-xs text-rose-600 md:col-span-2">{error}</p>}

          <button type="submit" className="md:col-span-2 rounded-xl bg-gradient-to-r from-teal-600 to-emerald-500 py-2.5 font-bold text-white shadow-lg hover:opacity-90 transition">
            Create Account
          </button>
        </form>

        {isFirebaseAuthConfigured() && (
          <div className="mt-4">
            <div className="flex items-center gap-2 mb-2">
              <hr className="flex-1 border-slate-200" />
              <span className="text-xs text-slate-400">or</span>
              <hr className="flex-1 border-slate-200" />
            </div>
            <button type="button" onClick={handleFirebaseGoogleSignup}
              className="w-full rounded-xl border border-orange-200 bg-orange-50 py-2.5 text-sm font-semibold text-orange-700 hover:bg-orange-100 transition">
              Sign up with Google (Firebase)
            </button>
          </div>
        )}

        <p className="mt-5 text-center text-sm text-slate-500">
          Already have an account? <Link to="/login" className="font-semibold text-teal-700 hover:text-teal-600">Sign in</Link>
        </p>
      </div>
    </div>
  );
};
