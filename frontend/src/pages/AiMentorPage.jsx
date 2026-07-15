import { useEffect, useRef, useState } from "react";
import api from "../lib/api";

// ─── Constants ────────────────────────────────────────────────────────────────

const SESSION_KEY = "skillora_ai_sessions_v3";

const AI_MODES = [
  { id: "LEARN",     icon: "📚", title: "Learn",     desc: "Deep concept explanations",     grad: "from-teal-600 to-cyan-600",    ring: "ring-teal-400",   bg: "bg-teal-50",   border: "border-teal-400",  text: "text-teal-900"  },
  { id: "PRACTICE",  icon: "✍️",  title: "Practice",  desc: "Solve MCQs with explanations",  grad: "from-sky-600 to-blue-600",     ring: "ring-sky-400",    bg: "bg-sky-50",    border: "border-sky-400",   text: "text-sky-900"   },
  { id: "ADAPTIVE",  icon: "🎯",  title: "Adaptive",  desc: "AI adjusts to your level",      grad: "from-purple-600 to-violet-600", ring: "ring-purple-400", bg: "bg-purple-50", border: "border-purple-400",text: "text-purple-900"},
  { id: "REVISION",  icon: "⚡",  title: "Revision",  desc: "Formula sheets & cheatsheets",  grad: "from-amber-500 to-orange-500",  ring: "ring-amber-400",  bg: "bg-amber-50",  border: "border-amber-400", text: "text-amber-900" },
  { id: "MOCK_TEST", icon: "🏆",  title: "Mock Test", desc: "20 Aptitude + 2 Coding",        grad: "from-rose-600 to-pink-600",     ring: "ring-rose-400",   bg: "bg-rose-50",   border: "border-rose-400",  text: "text-rose-900"  },
];

// Comprehensive topic-wise curriculum
const CURRICULUM = {
  "📊 Quantitative Aptitude": {
    color: "bg-blue-50 border-blue-200",
    badge: "bg-blue-100 text-blue-800",
    topics: [
      { name: "Percentages", subtopics: ["Successive change", "Percentage of percentage", "Population problems"] },
      { name: "Profit & Loss", subtopics: ["Discount problems", "Marked price", "Partnership profit"] },
      { name: "Time & Work", subtopics: ["Pipe & cistern", "Efficiency problems", "Negative work"] },
      { name: "Speed, Distance & Time", subtopics: ["Relative speed", "Train problems", "Boats & streams"] },
      { name: "HCF & LCM", subtopics: ["Bells problem", "Tile/box problems"] },
      { name: "Averages & Mixtures", subtopics: ["Weighted average", "Alligation method"] },
      { name: "Ratio & Proportion", subtopics: ["Compound ratio", "Partnership"] },
      { name: "Simple & Compound Interest", subtopics: ["CI vs SI difference", "Installment problems"] },
      { name: "Permutation & Combination", subtopics: ["Word formation", "Selection problems"] },
      { name: "Probability", subtopics: ["Bag & ball", "Card problems", "Dice problems"] },
      { name: "Number Systems", subtopics: ["Divisibility rules", "Remainder theorem", "Unit digit"] },
      { name: "Geometry & Mensuration", subtopics: ["Area, Perimeter", "Volumes", "Coordinate geometry"] },
    ],
  },
  "🧠 Logical Reasoning": {
    color: "bg-purple-50 border-purple-200",
    badge: "bg-purple-100 text-purple-800",
    topics: [
      { name: "Syllogisms", subtopics: ["Venn diagram method", "All/Some/No statements"] },
      { name: "Blood Relations", subtopics: ["Family tree", "Coded relations"] },
      { name: "Coding & Decoding", subtopics: ["Letter coding", "Number coding", "Symbol substitution"] },
      { name: "Direction Sense", subtopics: ["Shadow problems", "Distance calculation"] },
      { name: "Seating Arrangements", subtopics: ["Circular", "Linear", "Complex arrangements"] },
      { name: "Data Interpretation", subtopics: ["Bar chart", "Pie chart", "Line graph", "Table DI"] },
      { name: "Puzzles & Grids", subtopics: ["Floor puzzle", "Box puzzle", "Scheduling"] },
    ],
  },
  "💻 Data Structures & Algorithms": {
    color: "bg-green-50 border-green-200",
    badge: "bg-green-100 text-green-800",
    topics: [
      { name: "Arrays", subtopics: ["Two pointer", "Sliding window", "Prefix sum", "Kadane's algorithm"] },
      { name: "Linked Lists", subtopics: ["Reversal", "Cycle detection", "Merge sorted lists"] },
      { name: "Stacks & Queues", subtopics: ["Valid parentheses", "Monotonic stack", "Circular queue"] },
      { name: "Binary Trees", subtopics: ["Traversals", "Height/Depth", "LCA", "Diameter"] },
      { name: "Binary Search Trees", subtopics: ["Insert/Delete", "Range queries", "Balanced BST"] },
      { name: "Graphs", subtopics: ["BFS/DFS", "Topological sort", "Dijkstra", "Union-Find"] },
      { name: "Dynamic Programming", subtopics: ["Memoization", "Tabulation", "Knapsack", "LCS/LIS"] },
      { name: "Sorting Algorithms", subtopics: ["Merge sort", "Quick sort", "Heap sort", "Counting sort"] },
      { name: "Binary Search", subtopics: ["On arrays", "On answers", "Rotated arrays"] },
      { name: "Hashing", subtopics: ["HashMap patterns", "Two sum", "Anagram problems"] },
      { name: "Recursion & Backtracking", subtopics: ["N-Queens", "Subsets", "Permutations"] },
      { name: "Heaps & Priority Queues", subtopics: ["Top-K problems", "Median finding"] },
    ],
  },
  "🗄️ CS Fundamentals": {
    color: "bg-orange-50 border-orange-200",
    badge: "bg-orange-100 text-orange-800",
    topics: [
      { name: "SQL & DBMS", subtopics: ["Joins", "Aggregation", "Indexes", "Transactions", "Normalization"] },
      { name: "OOPS Concepts", subtopics: ["Inheritance", "Polymorphism", "Encapsulation", "Abstraction"] },
      { name: "Operating Systems", subtopics: ["Process management", "Memory management", "Deadlock", "Scheduling"] },
      { name: "Computer Networks", subtopics: ["OSI model", "TCP/IP", "DNS", "HTTP/HTTPS"] },
      { name: "System Design Basics", subtopics: ["Scalability", "Load balancing", "Caching", "Databases"] },
    ],
  },
};

const COMPANIES = [
  "Any Company", "TCS", "Infosys", "Wipro", "Cognizant", "Capgemini",
  "Accenture", "HCL", "Tech Mahindra", "Amazon", "Zoho", "Freshworks"
];

const LEVELS = ["Beginner", "Intermediate", "Advanced"];
const DIFFICULTIES = ["Easy", "Medium", "Hard", "Adaptive"];
const MODE_TIPS = {
  LEARN:     ["Explain the concept first, then ask for examples", "Try 'Binary Trees — Inorder traversal'", "Ask for real-world analogies", "Great for first-time learning"],
  PRACTICE:  ["Set 10–15 questions for a solid session", "Use Medium for campus prep", "After practice, switch to Adaptive mode", "One topic at a time works best"],
  ADAPTIVE:  ["Enter your recent test scores for personalization", "AI increases difficulty as you improve", "Best used after Practice sessions", "Review weak topics in Learn mode first"],
  REVISION:  ["Use the night before an exam for quick recap", "Add subtopic for focused formula sheets", "Best paired with a 5-minute review", "Ask for common traps and tricks"],
  MOCK_TEST: ["Always get 20 Aptitude + 2 Coding questions", "Time yourself — 90 min total", "Review all wrong answers in REVISION mode after", "Simulate real placement conditions"],
};

// ─── Markdown Renderer ────────────────────────────────────────────────────────

function renderInline(text) {
  const parts = text.split(/\*\*(.*?)\*\*/g);
  return parts.map((p, i) => i % 2 === 1 ? <strong key={i}>{p}</strong> : p);
}

function MarkdownRenderer({ content }) {
  const lines = content.split("\n");
  const els = [];
  let bullets = [], ordered = [], k = 0;
  let inCode = false, codeLines = [];

  const flushB = () => { if (bullets.length) { els.push(<ul key={k++} className="my-2 list-disc pl-5 space-y-1">{bullets.map((t,i)=><li key={i} className="text-sm leading-relaxed">{renderInline(t)}</li>)}</ul>); bullets=[]; }};
  const flushO = () => { if (ordered.length) { els.push(<ol key={k++} className="my-2 list-decimal pl-5 space-y-1">{ordered.map((t,i)=><li key={i} className="text-sm leading-relaxed">{renderInline(t)}</li>)}</ol>); ordered=[]; }};
  const flushCode = () => { if (codeLines.length) { els.push(<pre key={k++} className="my-3 overflow-x-auto rounded-xl bg-slate-900 p-4 text-xs text-slate-100"><code>{codeLines.join("\n")}</code></pre>); codeLines=[]; inCode=false; }};

  for (const line of lines) {
    const t = line.trim();
    if (t.startsWith("```")) {
      if (inCode) flushCode();
      else { flushB(); flushO(); inCode = true; }
      continue;
    }
    if (inCode) { codeLines.push(line); continue; }
    if (t.startsWith("### ")) { flushB(); flushO(); els.push(<h4 key={k++} className="mt-5 mb-1 text-base font-bold text-slate-900 flex items-center gap-2">{renderInline(t.slice(4))}</h4>); }
    else if (t.startsWith("## "))  { flushB(); flushO(); els.push(<h3 key={k++} className="mt-6 mb-2 text-lg font-bold text-slate-900 border-b border-slate-200 pb-1">{renderInline(t.slice(3))}</h3>); }
    else if (t.startsWith("# "))   { flushB(); flushO(); els.push(<h2 key={k++} className="mt-6 mb-2 text-xl font-bold text-slate-900">{renderInline(t.slice(2))}</h2>); }
    else if (t.startsWith("---"))  { flushB(); flushO(); els.push(<hr key={k++} className="my-4 border-slate-200" />); }
    else if (/^[-•] /.test(t))     { flushO(); bullets.push(t.replace(/^[-•] /, "")); }
    else if (/^\d+\. /.test(t))    { flushB(); ordered.push(t.replace(/^\d+\. /, "")); }
    else if (t === "")             { flushB(); flushO(); els.push(<div key={k++} className="h-2" />); }
    else                           { flushB(); flushO(); els.push(<p key={k++} className="text-sm leading-relaxed text-slate-700">{renderInline(t)}</p>); }
  }
  flushB(); flushO(); if (inCode) flushCode();
  return <div className="space-y-1">{els}</div>;
}

function Skeleton() {
  return (
    <div className="space-y-4 animate-pulse">
      <div className="h-5 w-1/3 rounded-lg bg-slate-200" />
      <div className="space-y-2">
        <div className="h-3 w-full rounded bg-slate-200" />
        <div className="h-3 w-5/6 rounded bg-slate-200" />
        <div className="h-3 w-4/5 rounded bg-slate-200" />
      </div>
      <div className="h-5 w-1/4 rounded-lg bg-slate-200 mt-4" />
      <div className="space-y-2">
        <div className="h-3 w-full rounded bg-slate-200" />
        <div className="h-3 w-3/4 rounded bg-slate-200" />
      </div>
      <div className="h-24 w-full rounded-xl bg-slate-200 mt-2" />
    </div>
  );
}

const loadSessions = () => { try { return JSON.parse(localStorage.getItem(SESSION_KEY) || "[]"); } catch { return []; } };
const saveSessions = (s) => localStorage.setItem(SESSION_KEY, JSON.stringify(s));

// ─── Main Component ───────────────────────────────────────────────────────────

export const AiMentorPage = () => {
  const [mode, setMode] = useState("LEARN");
  const [topic, setTopic] = useState("");
  const [subtopic, setSubtopic] = useState("");
  const [level, setLevel] = useState("Intermediate");
  const [difficulty, setDifficulty] = useState("Medium");
  const [numQuestions, setNumQuestions] = useState(10);
  const [company, setCompany] = useState("Any Company");
  const [correct, setCorrect] = useState(0);
  const [wrong, setWrong] = useState(0);
  const [accuracy, setAccuracy] = useState(0);
  const [curriculumOpen, setCurriculumOpen] = useState(null); // which category is expanded
  const [content, setContent] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [copied, setCopied] = useState(false);
  const [sessions, setSessions] = useState(loadSessions);
  const [aiGenerated, setAiGenerated] = useState(true);
  const topicRef = useRef(null);
  const outputRef = useRef(null);

  const modeObj = AI_MODES.find(m => m.id === mode);

  const generate = async () => {
    if (!topic.trim()) { setError("Please enter a topic first."); topicRef.current?.focus(); return; }
    setLoading(true); setError(""); setContent("");
    try {
      const res = await api.post("/api/student/ai-mentor", {
        mode, topic, subtopic, difficulty,
        numberOfQuestions: numQuestions,
        studentLevel: level,
        company: company === "Any Company" ? null : company,
        weakTopics: [],
        previousTopicsCovered: [],
        previouslyGeneratedQuestions: [],
        correct, wrong, accuracy,
      });
      const c = res.data?.content;
      setAiGenerated(res.data?.aiGenerated ?? true);
      if (c && c.trim()) {
        setContent(c);
        const entry = { mode, topic, subtopic, company, timestamp: new Date().toISOString() };
        const updated = [entry, ...sessions].slice(0, 12);
        setSessions(updated);
        saveSessions(updated);
        setTimeout(() => outputRef.current?.scrollIntoView({ behavior: "smooth" }), 100);
      } else {
        setError("AI returned empty content. Check backend and Groq API key.");
      }
    } catch (e) {
      setError(e.response?.data?.message || "Failed to generate. Ensure backend is running.");
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

  const selectTopic = (t, sub = "") => {
    setTopic(t);
    if (sub) setSubtopic(sub);
    topicRef.current?.focus();
  };

  return (
    <div className="space-y-6" onKeyDown={handleKey}>

      {/* ── Premium Banner ── */}
      <div style={{background: "linear-gradient(135deg, #0f172a 0%, #1e3a5f 50%, #0e7490 100%)"}} className="rounded-2xl p-6 text-white relative overflow-hidden">
        <div className="absolute inset-0 opacity-10" style={{backgroundImage: "radial-gradient(circle at 30% 50%, #06b6d4 0%, transparent 50%), radial-gradient(circle at 80% 20%, #8b5cf6 0%, transparent 40%)"}} />
        <div className="relative">
          <div className="flex items-center gap-3">
            <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-white/10 backdrop-blur-sm border border-white/20 text-2xl">🤖</div>
            <div>
              <p className="text-xs font-semibold uppercase tracking-widest text-cyan-300">AI-Powered Learning</p>
              <h2 className="text-2xl font-bold">Skillora AI Mentor</h2>
            </div>
          </div>
          <p className="mt-3 text-sm text-slate-300 max-w-xl">
            Your personal placement coach powered by <span className="font-semibold text-cyan-300">Groq Llama 3.3 70B</span> — master aptitude, DSA, and CS fundamentals with AI-tailored learning paths.
          </p>
          <div className="mt-4 flex flex-wrap gap-2">
            {["🎯 Topic-wise Learning", "📊 Adaptive Practice", "🏆 Mock Tests (20+2)", "⚡ Instant Revision Sheets"].map(tag => (
              <span key={tag} className="rounded-full bg-white/10 border border-white/20 px-3 py-1 text-xs font-medium text-white/90">{tag}</span>
            ))}
          </div>
        </div>
      </div>

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
            {mode === m.id && <div className={`absolute inset-0 bg-gradient-to-br ${m.grad} opacity-5`} />}
            <div className="relative">
              <div className="mb-2 text-2xl">{m.icon}</div>
              <p className="font-bold text-slate-900 text-sm">{m.title}</p>
              <p className="mt-0.5 text-[11px] text-slate-500 leading-tight">{m.desc}</p>
            </div>
          </button>
        ))}
      </div>

      {/* ── Main Layout ── */}
      <div className="grid gap-6 lg:grid-cols-[1fr_300px]">

        {/* LEFT: Input + Output */}
        <div className="space-y-5">

          {/* Input Card */}
          <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
            <div className="mb-4 flex items-center gap-2">
              <span className={`inline-flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br ${modeObj.grad} text-white text-lg`}>{modeObj.icon}</span>
              <div>
                <h3 className="font-bold text-slate-900">{modeObj.title} Mode</h3>
                <p className="text-xs text-slate-500">{modeObj.desc}</p>
              </div>
            </div>

            <div className="space-y-4">

              {/* Topic Row */}
              <div className="grid gap-3 sm:grid-cols-2">
                <div>
                  <label className="mb-1.5 block text-sm font-semibold text-slate-700">Topic <span className="text-rose-500">*</span></label>
                  <input
                    ref={topicRef}
                    type="text"
                    value={topic}
                    onChange={e => setTopic(e.target.value)}
                    placeholder="e.g., Binary Trees, Percentages..."
                    className="w-full rounded-xl border border-slate-200 px-4 py-2.5 text-sm focus:border-teal-500 focus:outline-none focus:ring-2 focus:ring-teal-500/20 transition"
                  />
                </div>
                <div>
                  <label className="mb-1.5 block text-sm font-semibold text-slate-700">Subtopic <span className="text-slate-400 font-normal">(optional)</span></label>
                  <input
                    type="text"
                    value={subtopic}
                    onChange={e => setSubtopic(e.target.value)}
                    placeholder="e.g., Inorder traversal, Remainder problems..."
                    className="w-full rounded-xl border border-slate-200 px-4 py-2.5 text-sm focus:border-teal-500 focus:outline-none focus:ring-2 focus:ring-teal-500/20 transition"
                  />
                </div>
              </div>

              {/* Level + Company */}
              <div className="grid gap-3 sm:grid-cols-2">
                <div>
                  <label className="mb-2 block text-sm font-semibold text-slate-700">Student Level</label>
                  <div className="flex gap-2">
                    {LEVELS.map(l => (
                      <button key={l} onClick={() => setLevel(l)}
                        className={`flex-1 rounded-xl py-2 text-xs font-semibold transition ${level === l ? "bg-teal-700 text-white shadow" : "bg-slate-100 text-slate-600 hover:bg-slate-200"}`}>
                        {l}
                      </button>
                    ))}
                  </div>
                </div>
                <div>
                  <label className="mb-1.5 block text-sm font-semibold text-slate-700">Target Company</label>
                  <select
                    value={company}
                    onChange={e => setCompany(e.target.value)}
                    className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm outline-none focus:border-teal-500 focus:ring-2 focus:ring-teal-500/20"
                  >
                    {COMPANIES.map(c => <option key={c}>{c}</option>)}
                  </select>
                </div>
              </div>

              {/* PRACTICE / MOCK controls */}
              {(mode === "PRACTICE" || mode === "MOCK_TEST") && (
                <div className={`rounded-xl p-4 ${mode === "MOCK_TEST" ? "bg-rose-50 border border-rose-200" : "bg-sky-50 border border-sky-200"}`}>
                  {mode === "MOCK_TEST" && (
                    <div className="mb-3 flex items-center gap-2">
                      <span className="text-lg">🏆</span>
                      <div>
                        <p className="text-sm font-bold text-rose-800">Mock Test Format</p>
                        <p className="text-xs text-rose-600">20 Aptitude MCQs + 2 Coding Problems (1 Easy + 1 Medium) · 90 minutes</p>
                      </div>
                    </div>
                  )}
                  <div className="grid gap-4 sm:grid-cols-2">
                    {mode === "PRACTICE" && (
                      <div>
                        <label className="mb-1.5 block text-sm font-semibold text-slate-700">Questions: {numQuestions}</label>
                        <input type="range" min={5} max={30} value={numQuestions} onChange={e => setNumQuestions(+e.target.value)} className="w-full" />
                      </div>
                    )}
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
                </div>
              )}

              {/* ADAPTIVE performance inputs */}
              {mode === "ADAPTIVE" && (
                <div className="rounded-xl bg-purple-50 border border-purple-200 p-4">
                  <p className="mb-3 text-sm font-bold text-purple-900">📊 Your Recent Performance</p>
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
                className={`w-full rounded-xl bg-gradient-to-r ${modeObj.grad} px-6 py-3.5 font-bold text-white shadow-lg transition hover:opacity-90 disabled:opacity-60 disabled:cursor-not-allowed ${loading ? "animate-pulse" : ""}`}>
                {loading ? `🔄 Generating ${modeObj.title}...` : `✨ Generate ${modeObj.title}`}
              </button>

              <p className="text-center text-xs text-slate-400">Press ⌘↵ or Ctrl+↵ to generate</p>
            </div>
          </div>

          {/* Output Card */}
          {(loading || content) && (
            <div ref={outputRef} className={`rounded-2xl border-2 overflow-hidden ${modeObj.border}`}>
              {/* Output header */}
              <div className={`flex items-center justify-between px-5 py-4 bg-gradient-to-r ${modeObj.grad}`}>
                <div className="flex items-center gap-3">
                  <span className="text-2xl">{modeObj.icon}</span>
                  <div>
                    <p className="text-sm font-bold text-white">{modeObj.title} — {topic}</p>
                    {subtopic && <p className="text-xs text-white/70">{subtopic}</p>}
                    {company && company !== "Any Company" && <p className="text-xs text-white/60">🏢 {company} style</p>}
                  </div>
                </div>
                {content && (
                  <div className="flex items-center gap-2">
                    <span className={`rounded-full px-2.5 py-1 text-[10px] font-bold ${aiGenerated ? "bg-green-400/30 text-green-100" : "bg-amber-400/30 text-amber-100"}`}>
                      {aiGenerated ? "🤖 AI" : "📄 Template"}
                    </span>
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
              <div className={`p-6 ${modeObj.bg}`}>
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

          {/* Curriculum Navigator */}
          <div className="rounded-2xl border border-slate-200 bg-white p-4">
            <h3 className="mb-3 text-sm font-bold text-slate-900">📚 Topic Curriculum</h3>
            <div className="space-y-2">
              {Object.entries(CURRICULUM).map(([category, data]) => (
                <div key={category}>
                  <button
                    onClick={() => setCurriculumOpen(curriculumOpen === category ? null : category)}
                    className={`w-full flex items-center justify-between rounded-xl border px-3 py-2 text-left text-xs font-bold transition ${data.color} hover:opacity-90`}
                  >
                    <span>{category}</span>
                    <span className="text-slate-500">{curriculumOpen === category ? "▲" : "▼"}</span>
                  </button>
                  {curriculumOpen === category && (
                    <div className="mt-1 space-y-1 pl-2">
                      {data.topics.map(t => (
                        <div key={t.name} className="rounded-xl border border-slate-100 bg-slate-50 p-2">
                          <button
                            onClick={() => selectTopic(t.name)}
                            className="w-full text-left text-xs font-semibold text-slate-800 hover:text-teal-700 transition"
                          >
                            {t.name}
                          </button>
                          <div className="mt-1 flex flex-wrap gap-1">
                            {t.subtopics.map(sub => (
                              <button
                                key={sub}
                                onClick={() => selectTopic(t.name, sub)}
                                className="rounded-full bg-white border border-slate-200 px-2 py-0.5 text-[10px] text-slate-500 hover:border-teal-400 hover:text-teal-700 transition"
                              >
                                {sub}
                              </button>
                            ))}
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>

          {/* Recent Sessions */}
          <div className="rounded-2xl border border-slate-200 bg-white p-4">
            <div className="mb-3 flex items-center justify-between">
              <h3 className="text-sm font-bold text-slate-900">🕐 Recent Sessions</h3>
              {sessions.length > 0 && (
                <button onClick={clearSessions} className="text-xs text-slate-400 hover:text-rose-500 transition">Clear</button>
              )}
            </div>
            {sessions.length === 0 ? (
              <p className="text-xs text-slate-400 text-center py-4">No sessions yet. Generate your first!</p>
            ) : (
              <div className="space-y-1.5 max-h-48 overflow-y-auto">
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
