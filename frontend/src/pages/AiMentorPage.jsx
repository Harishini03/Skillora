import { useEffect, useRef, useState } from "react";
import api from "../lib/api";

// ─── Constants ────────────────────────────────────────────────────────────────

const SESSION_KEY = "skillora_ai_sessions";

const AI_MODES = [
  { id: "LEARN",     icon: "📚", title: "Learn",     desc: "Master concepts with deep explanations",    grad: "from-teal-600 to-cyan-600",    ring: "ring-teal-400",   bg: "bg-teal-50",   border: "border-teal-400",  text: "text-teal-900"  },
  { id: "PRACTICE",  icon: "✍️",  title: "Practice",  desc: "Solve problems with instant feedback",      grad: "from-sky-600 to-blue-600",     ring: "ring-sky-400",    bg: "bg-sky-50",    border: "border-sky-400",   text: "text-sky-900"   },
  { id: "ADAPTIVE",  icon: "🎯",  title: "Adaptive",  desc: "AI adjusts to your skill level",           grad: "from-purple-600 to-violet-600", ring: "ring-purple-400", bg: "bg-purple-50", border: "border-purple-400",text: "text-purple-900"},
  { id: "REVISION",  icon: "⚡",  title: "Revision",  desc: "Quick summaries before your exam",         grad: "from-amber-500 to-orange-500",  ring: "ring-amber-400",  bg: "bg-amber-50",  border: "border-amber-400", text: "text-amber-900" },
  { id: "MOCK_TEST", icon: "🏆",  title: "Mock Test", desc: "Simulate real placement test conditions",   grad: "from-rose-600 to-pink-600",     ring: "ring-rose-400",   bg: "bg-rose-50",   border: "border-rose-400",  text: "text-rose-900"  },
];

const MODE_TIPS = {
  LEARN:     ["Explain the concept first, then ask for examples", "Try 'HCF' or 'Binary Trees'", "Ask for real-world analogies", "Request step-by-step derivation"],
  PRACTICE:  ["Set questions to 10-15 for a good session", "Use Medium difficulty for campus prep", "After practice, switch to Adaptive", "Focus on one topic at a time"],
  ADAPTIVE:  ["Enter your recent test scores for personalization", "AI increases difficulty as you improve", "Best used after a Practice session", "Review weak topics in Learn mode first"],
  REVISION:  ["Use before exam day for quick recap", "Add subtopic for focused summary", "Ask for formula sheets", "Best paired with 5-minute review"],
  MOCK_TEST: ["Set 20+ questions to simulate a real test", "Use Hard difficulty for placement prep", "Time yourself — 1 min per question", "Review all answers in Revision mode after"],
};

const QUICK_TOPICS = {
  "Aptitude":        ["HCF & LCM", "Percentages", "Time & Work", "Probability", "Profit & Loss", "Time & Distance"],
  "DSA":             ["Arrays", "Binary Trees", "Dynamic Programming", "Graphs", "Stacks & Queues", "Sorting"],
  "CS Fundamentals": ["DBMS", "Operating Systems", "OOPS Concepts", "Computer Networks", "SQL Joins"],
};

const LEVELS = ["Beginner", "Intermediate", "Advanced"];
const DIFFICULTIES = ["Easy", "Medium", "Hard", "Adaptive"];

// ─── Markdown Renderer ────────────────────────────────────────────────────────

function renderInline(text) {
  const parts = text.split(/\*\*(.*?)\*\*/g);
  return parts.map((p, i) => i % 2 === 1 ? <strong key={i}>{p}</strong> : p);
}

function MarkdownRenderer({ content }) {
  const lines = content.split("\n");
  const els = [];
  let bullets = [], ordered = [], k = 0;

  const flushB = () => { if (bullets.length) { els.push(<ul key={k++} className="my-2 list-disc pl-5 space-y-1">{bullets.map((t,i)=><li key={i} className="text-sm leading-relaxed">{renderInline(t)}</li>)}</ul>); bullets=[]; }};
  const flushO = () => { if (ordered.length) { els.push(<ol key={k++} className="my-2 list-decimal pl-5 space-y-1">{ordered.map((t,i)=><li key={i} className="text-sm leading-relaxed">{renderInline(t)}</li>)}</ol>); ordered=[]; }};

  for (const line of lines) {
    const t = line.trim();
    if (t.startsWith("### ")) { flushB(); flushO(); els.push(<h4 key={k++} className="mt-4 mb-1 text-base font-bold text-slate-900">{renderInline(t.slice(4))}</h4>); }
    else if (t.startsWith("## ")) { flushB(); flushO(); els.push(<h3 key={k++} className="mt-5 mb-2 text-lg font-bold text-slate-900">{renderInline(t.slice(3))}</h3>); }
    else if (t.startsWith("# "))  { flushB(); flushO(); els.push(<h2 key={k++} className="mt-6 mb-2 text-xl font-bold text-slate-900">{renderInline(t.slice(2))}</h2>); }
    else if (/^[-•] /.test(t))   { flushO(); bullets.push(t.replace(/^[-•] /, "")); }
    else if (/^\d+\. /.test(t))  { flushB(); ordered.push(t.replace(/^\d+\. /, "")); }
    else if (t === "")            { flushB(); flushO(); els.push(<div key={k++} className="h-2" />); }
    else                          { flushB(); flushO(); els.push(<p key={k++} className="text-sm leading-relaxed text-slate-700">{renderInline(t)}</p>); }
  }
  flushB(); flushO();
  return <div className="space-y-1">{els}</div>;
}

// ─── Loading Skeleton ─────────────────────────────────────────────────────────

function Skeleton() {
  return (
    <div className="space-y-3 animate-pulse">
      <div className="h-4 w-3/4 rounded bg-slate-200" />
      <div className="h-4 w-full rounded bg-slate-200" />
      <div className="h-4 w-5/6 rounded bg-slate-200" />
      <div className="h-4 w-2/3 rounded bg-slate-200" />
      <div className="h-4 w-full rounded bg-slate-200" />
      <div className="h-4 w-4/5 rounded bg-slate-200" />
    </div>
  );
}

// ─── Session helpers ──────────────────────────────────────────────────────────

const loadSessions = () => { try { return JSON.parse(localStorage.getItem(SESSION_KEY) || "[]"); } catch { return []; } };
const saveSessions = (sessions) => localStorage.setItem(SESSION_KEY, JSON.stringify(sessions));

// ─── Main Component ───────────────────────────────────────────────────────────

export const AiMentorPage = () => {
  const [mode, setMode] = useState("LEARN");
  const [topic, setTopic] = useState("");
  const [subtopic, setSubtopic] = useState("");
  const [level, setLevel] = useState("Intermediate");
  const [difficulty, setDifficulty] = useState("Medium");
  const [numQuestions, setNumQuestions] = useState(10);
  const [correct, setCorrect] = useState(0);
  const [wrong, setWrong] = useState(0);
  const [accuracy, setAccuracy] = useState(0);

  const [content, setContent] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [copied, setCopied] = useState(false);

  const [sessions, setSessions] = useState(loadSessions);
  const topicRef = useRef(null);
  const outputRef = useRef(null);

  const modeObj = AI_MODES.find(m => m.id === mode);

  const generate = async () => {
    if (!topic.trim()) { setError("Please enter a topic first."); topicRef.current?.focus(); return; }
    setLoading(true); setError(""); setContent("");
    try {
      const res = await api.post("/api/student/ai-mentor", {
        mode, topic, subtopic, difficulty, numberOfQuestions: numQuestions,
        studentLevel: level, weakTopics: [], previousTopicsCovered: [],
        previouslyGeneratedQuestions: [], correct, wrong, accuracy,
      });
      const c = res.data?.content;
      if (c && c.trim()) {
        setContent(c);
        const entry = { mode, topic, subtopic, timestamp: new Date().toISOString() };
        const updated = [entry, ...sessions].slice(0, 8);
        setSessions(updated);
        saveSessions(updated);
        setTimeout(() => outputRef.current?.scrollIntoView({ behavior: "smooth" }), 100);
      } else {
        setError("AI returned empty content. Check your Groq API key is configured in application.properties.");
      }
    } catch (e) {
      setError(e.response?.data?.message || "Failed to generate. Check backend is running and Groq API key is set.");
    } finally {
      setLoading(false);
    }
  };

  const copyContent = async () => {
    await navigator.clipboard.writeText(content);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const clearSessions = () => { setSessions([]); localStorage.removeItem(SESSION_KEY); };

  const loadSession = (s) => { setMode(s.mode); setTopic(s.topic); setSubtopic(s.subtopic || ""); };

  const handleKey = (e) => { if (e.key === "Enter" && e.metaKey) generate(); };

  return (
    <div className="space-y-6" onKeyDown={handleKey}>

      {/* ── Banner ── */}
      <section className="portal-banner">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-100">AI Learning System</p>
        <h2 className="mt-2 text-3xl font-bold text-white">Skillora AI Mentor</h2>
        <p className="mt-2 text-sm text-cyan-50/90">
          Powered by Groq · Personalized for placement preparation · Press ⌘↵ to generate
        </p>
      </section>

      {/* ── Mode Selector ── */}
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-5">
        {AI_MODES.map(m => (
          <button
            key={m.id}
            onClick={() => setMode(m.id)}
            className={`group relative overflow-hidden rounded-2xl border-2 p-4 text-left transition-all duration-200 ${
              mode === m.id
                ? `${m.border} ${m.bg} shadow-lg scale-[1.02] ring-2 ${m.ring} ring-offset-1`
                : "border-slate-200 bg-white hover:border-slate-300 hover:shadow-md"
            }`}
          >
            {mode === m.id && (
              <div className={`absolute inset-0 bg-gradient-to-br ${m.grad} opacity-5`} />
            )}
            <div className="relative">
              <div className="mb-2 text-2xl">{m.icon}</div>
              <p className="font-bold text-slate-900 text-sm">{m.title}</p>
              <p className="mt-0.5 text-[11px] text-slate-500 leading-tight">{m.desc}</p>
            </div>
          </button>
        ))}
      </div>

      {/* ── Main Layout ── */}
      <div className="grid gap-6 lg:grid-cols-[1fr_280px]">

        {/* LEFT: Input + Output */}
        <div className="space-y-5">

          {/* Input Card */}
          <div className="dashboard-card p-5">
            <div className="mb-4 flex items-center gap-2">
              <span className={`inline-flex h-8 w-8 items-center justify-center rounded-xl bg-gradient-to-br ${modeObj.grad} text-white text-base`}>{modeObj.icon}</span>
              <h3 className="font-bold text-slate-900">{modeObj.title} Mode — Configure Session</h3>
            </div>

            <div className="space-y-4">

              {/* Topic */}
              <div>
                <label className="mb-1.5 block text-sm font-semibold text-slate-700">Topic <span className="text-rose-500">*</span></label>
                <input
                  ref={topicRef}
                  type="text"
                  value={topic}
                  onChange={e => setTopic(e.target.value)}
                  placeholder="e.g., HCF & LCM, Binary Trees, SQL Joins, Percentages..."
                  className="w-full rounded-xl border border-slate-200 px-4 py-2.5 text-sm focus:border-teal-500 focus:outline-none focus:ring-2 focus:ring-teal-500/20 transition"
                />
              </div>

              {/* Subtopic */}
              <div>
                <label className="mb-1.5 block text-sm font-semibold text-slate-700">Subtopic <span className="text-slate-400 font-normal">(optional)</span></label>
                <input
                  type="text"
                  value={subtopic}
                  onChange={e => setSubtopic(e.target.value)}
                  placeholder="e.g., Remainder problems, Preorder traversal, GROUP BY..."
                  className="w-full rounded-xl border border-slate-200 px-4 py-2.5 text-sm focus:border-teal-500 focus:outline-none focus:ring-2 focus:ring-teal-500/20 transition"
                />
              </div>

              {/* Level pills */}
              <div>
                <label className="mb-2 block text-sm font-semibold text-slate-700">Student Level</label>
                <div className="flex gap-2">
                  {LEVELS.map(l => (
                    <button key={l} onClick={() => setLevel(l)}
                      className={`rounded-full px-4 py-1.5 text-xs font-semibold transition ${level === l ? "bg-teal-700 text-white shadow" : "bg-slate-100 text-slate-600 hover:bg-slate-200"}`}>
                      {l}
                    </button>
                  ))}
                </div>
              </div>

              {/* PRACTICE / MOCK controls */}
              {(mode === "PRACTICE" || mode === "MOCK_TEST") && (
                <div className="grid gap-4 sm:grid-cols-2">
                  <div>
                    <label className="mb-1.5 block text-sm font-semibold text-slate-700">Questions</label>
                    <div className="flex items-center gap-2">
                      <input type="range" min={5} max={30} value={numQuestions} onChange={e => setNumQuestions(+e.target.value)} className="flex-1" />
                      <span className="w-8 text-center text-sm font-bold text-slate-900">{numQuestions}</span>
                    </div>
                  </div>
                  <div>
                    <label className="mb-2 block text-sm font-semibold text-slate-700">Difficulty</label>
                    <div className="flex flex-wrap gap-1.5">
                      {DIFFICULTIES.map(d => (
                        <button key={d} onClick={() => setDifficulty(d)}
                          className={`rounded-full px-3 py-1 text-xs font-semibold transition ${difficulty === d
                            ? d === "Easy" ? "bg-emerald-600 text-white"
                            : d === "Medium" ? "bg-amber-500 text-white"
                            : d === "Hard" ? "bg-rose-600 text-white"
                            : "bg-slate-700 text-white"
                            : "bg-slate-100 text-slate-600 hover:bg-slate-200"}`}>
                          {d}
                        </button>
                      ))}
                    </div>
                  </div>
                </div>
              )}

              {/* ADAPTIVE performance */}
              {mode === "ADAPTIVE" && (
                <div className="rounded-xl bg-purple-50 border border-purple-200 p-4">
                  <p className="mb-3 text-sm font-bold text-purple-900">Your Recent Performance (optional)</p>
                  <div className="grid grid-cols-3 gap-3">
                    {[["Correct ✓", correct, setCorrect, "text-emerald-700 bg-emerald-50 border-emerald-200"],
                      ["Wrong ✗", wrong, setWrong, "text-rose-700 bg-rose-50 border-rose-200"],
                      ["Accuracy %", accuracy, setAccuracy, "text-purple-700 bg-purple-50 border-purple-200"]
                    ].map(([label, val, setter, cls]) => (
                      <div key={label}>
                        <label className="mb-1 block text-xs font-semibold text-slate-600">{label}</label>
                        <input type="number" min={0} max={label.includes("%") ? 100 : undefined} value={val}
                          onChange={e => setter(+e.target.value || 0)}
                          className={`w-full rounded-lg border px-3 py-1.5 text-sm font-bold ${cls}`} />
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {error && (
                <div className="flex items-start gap-2 rounded-xl border border-rose-200 bg-rose-50 p-3">
                  <span className="text-rose-500 mt-0.5">⚠</span>
                  <p className="text-sm text-rose-700">{error}</p>
                </div>
              )}

              <button onClick={generate} disabled={loading}
                className={`w-full rounded-xl bg-gradient-to-r ${modeObj.grad} px-6 py-3 font-bold text-white shadow-lg transition hover:opacity-90 disabled:opacity-60 disabled:cursor-not-allowed ${loading ? "animate-pulse" : ""}`}>
                {loading ? `Generating ${modeObj.title}...` : `✨ Generate ${modeObj.title}`}
              </button>
            </div>
          </div>

          {/* Output Card */}
          {(loading || content || error) && (
            <div ref={outputRef} className={`dashboard-card overflow-hidden border-2 ${modeObj.border}`}>
              {/* Output header */}
              <div className={`flex items-center justify-between px-5 py-3 bg-gradient-to-r ${modeObj.grad}`}>
                <div className="flex items-center gap-2">
                  <span className="text-xl">{modeObj.icon}</span>
                  <div>
                    <p className="text-sm font-bold text-white">{modeObj.title} — {topic || "Session"}</p>
                    {subtopic && <p className="text-xs text-white/70">{subtopic}</p>}
                  </div>
                </div>
                {content && (
                  <div className="flex gap-2">
                    <button onClick={copyContent}
                      className="rounded-lg bg-white/20 px-3 py-1.5 text-xs font-semibold text-white hover:bg-white/30 transition">
                      {copied ? "✓ Copied!" : "Copy"}
                    </button>
                    <button onClick={generate}
                      className="rounded-lg bg-white/20 px-3 py-1.5 text-xs font-semibold text-white hover:bg-white/30 transition">
                      ↺ Regenerate
                    </button>
                  </div>
                )}
              </div>

              {/* Output body */}
              <div className={`p-5 ${modeObj.bg}`}>
                {loading && <Skeleton />}
                {!loading && content && (
                  <div className="prose prose-slate max-w-none">
                    <MarkdownRenderer content={content} />
                  </div>
                )}
              </div>
            </div>
          )}
        </div>

        {/* RIGHT: Sidebar */}
        <div className="space-y-4">

          {/* Quick Topics */}
          <div className="dashboard-card p-4">
            <h3 className="mb-3 text-sm font-bold text-slate-900">⚡ Quick Topics</h3>
            <div className="space-y-3">
              {Object.entries(QUICK_TOPICS).map(([cat, topics]) => (
                <div key={cat}>
                  <p className="mb-1.5 text-xs font-semibold uppercase tracking-wide text-slate-400">{cat}</p>
                  <div className="flex flex-wrap gap-1.5">
                    {topics.map(t => (
                      <button key={t} onClick={() => { setTopic(t); topicRef.current?.focus(); }}
                        className="rounded-full border border-slate-200 bg-white px-2.5 py-1 text-xs font-medium text-slate-700 hover:border-teal-400 hover:bg-teal-50 hover:text-teal-800 transition">
                        {t}
                      </button>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Recent Sessions */}
          <div className="dashboard-card p-4">
            <div className="mb-3 flex items-center justify-between">
              <h3 className="text-sm font-bold text-slate-900">🕐 Recent Sessions</h3>
              {sessions.length > 0 && (
                <button onClick={clearSessions} className="text-xs text-slate-400 hover:text-rose-500 transition">Clear</button>
              )}
            </div>
            {sessions.length === 0 ? (
              <p className="text-xs text-slate-400 text-center py-3">No sessions yet. Generate your first!</p>
            ) : (
              <div className="space-y-1.5">
                {sessions.map((s, i) => {
                  const m = AI_MODES.find(x => x.id === s.mode);
                  return (
                    <button key={i} onClick={() => loadSession(s)}
                      className="w-full rounded-xl border border-slate-100 bg-slate-50 p-2.5 text-left hover:border-teal-200 hover:bg-teal-50 transition">
                      <div className="flex items-center gap-2">
                        <span className="text-base">{m?.icon}</span>
                        <div className="flex-1 min-w-0">
                          <p className="truncate text-xs font-semibold text-slate-900">{s.topic}</p>
                          <p className="text-[10px] text-slate-400">{m?.title} · {new Date(s.timestamp).toLocaleDateString()}</p>
                        </div>
                      </div>
                    </button>
                  );
                })}
              </div>
            )}
          </div>

          {/* Mode Tips */}
          <div className={`rounded-2xl border ${modeObj.border} ${modeObj.bg} p-4`}>
            <h3 className={`mb-2.5 text-sm font-bold ${modeObj.text}`}>💡 {modeObj.title} Tips</h3>
            <ul className="space-y-1.5">
              {(MODE_TIPS[mode] || []).map((tip, i) => (
                <li key={i} className={`flex items-start gap-1.5 text-xs ${modeObj.text} opacity-90`}>
                  <span className="mt-0.5">•</span> {tip}
                </li>
              ))}
            </ul>
          </div>

        </div>
      </div>
    </div>
  );
};
