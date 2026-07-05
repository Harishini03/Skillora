import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../lib/api";

// ─── Difficulty helpers ───────────────────────────────────────────────────────
const DIFF_META = {
  EASY:   { label: "Easy",   cls: "text-emerald-600 bg-emerald-50 border-emerald-200" },
  MEDIUM: { label: "Medium", cls: "text-amber-600 bg-amber-50 border-amber-200"     },
  HARD:   { label: "Hard",   cls: "text-rose-600 bg-rose-50 border-rose-200"         },
};
const diffMeta = (d) => DIFF_META[d?.toUpperCase()] ?? { label: d ?? "—", cls: "text-slate-500 bg-slate-100 border-slate-200" };

// ─── Topic categories for filter pills ───────────────────────────────────────
const TOPIC_FILTERS = [
  "All", "Arrays", "Strings", "Dynamic Programming", "Stacks",
  "Linked Lists", "Sorting", "Searching", "Graphs", "Trees",
];

export const CodingProblemsPage = () => {
  const navigate = useNavigate();
  const [problems, setProblems]       = useState([]);
  const [loading, setLoading]         = useState(true);
  const [error, setError]             = useState("");
  const [search, setSearch]           = useState("");
  const [diffFilter, setDiffFilter]   = useState("ALL");
  const [topicFilter, setTopicFilter] = useState("All");

  useEffect(() => { loadProblems(); }, []);

  const loadProblems = async () => {
    setLoading(true); setError("");
    try {
      const res = await api.get("/api/coding/problems");
      setProblems(Array.isArray(res.data) ? res.data : []);
    } catch (e) {
      setError(e.response?.data?.message || "Failed to load problems. Make sure the backend is running.");
    } finally {
      setLoading(false);
    }
  };

  // ── Derived stats ──────────────────────────────────────────────────────────
  const stats = useMemo(() => ({
    total:  problems.length,
    easy:   problems.filter(p => p.difficultyLevel === "EASY").length,
    medium: problems.filter(p => p.difficultyLevel === "MEDIUM").length,
    hard:   problems.filter(p => p.difficultyLevel === "HARD").length,
    solved: problems.filter(p => p.isSolved).length,
  }), [problems]);

  // ── Filtered list ──────────────────────────────────────────────────────────
  const filtered = useMemo(() => {
    const q = search.toLowerCase();
    return problems.filter(p => {
      const matchDiff  = diffFilter === "ALL" || p.difficultyLevel === diffFilter;
      const matchTopic = topicFilter === "All" || (p.topicTags || "").toLowerCase().includes(topicFilter.toLowerCase());
      const matchSearch = !q || p.title.toLowerCase().includes(q) || (p.topicTags || "").toLowerCase().includes(q);
      return matchDiff && matchTopic && matchSearch;
    });
  }, [problems, diffFilter, topicFilter, search]);

  // ─────────────────────────────────────────────────────────────────────────
  return (
    <div className="space-y-5">

      {/* Banner */}
      <section className="portal-banner">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-100">DSA Practice</p>
        <h2 className="mt-1 text-3xl font-bold text-white">Problem Set</h2>
        <p className="mt-1 text-sm text-cyan-50/80">
          Practice data structures &amp; algorithms · Real interview problems · Run and submit code
        </p>
      </section>

      {/* Stat cards */}
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-5">
        {[
          { label:"Total",  val: stats.total,  numColor:"#0f172a", bg:"#f8fafc",  border:"#e2e8f0" },
          { label:"Easy",   val: stats.easy,   numColor:"#065f46", bg:"#f0fdf4",  border:"#bbf7d0" },
          { label:"Medium", val: stats.medium, numColor:"#78350f", bg:"#fffbeb",  border:"#fde68a" },
          { label:"Hard",   val: stats.hard,   numColor:"#7f1d1d", bg:"#fff1f2",  border:"#fecaca" },
          { label:"Solved", val: stats.solved, numColor:"#1e3a5f", bg:"#eff6ff",  border:"#bfdbfe" },
        ].map(s => (
          <div key={s.label} className="rounded-2xl border text-center px-4 py-3 transition hover:-translate-y-0.5 hover:shadow-md"
            style={{background:s.bg, borderColor:s.border}}>
            <p className="text-2xl font-black" style={{color:s.numColor,fontFamily:"var(--font-display)"}}>{s.val}</p>
            <p className="mt-0.5 text-xs font-semibold uppercase tracking-wide" style={{color:"#64748b"}}>{s.label}</p>
          </div>
        ))}
      </div>

      {/* Toolbar */}
      <div className="dashboard-card p-4 space-y-3">
        {/* Search */}
        <div className="relative">
          <svg className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <input
            type="text"
            placeholder="Search problems by title or topic..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="w-full rounded-xl border border-slate-200 pl-9 pr-4 py-2 text-sm focus:border-teal-500 focus:outline-none focus:ring-2 focus:ring-teal-500/20"
          />
        </div>

        {/* Difficulty filter */}
        <div className="flex flex-wrap gap-2">
          {[["ALL","All","#0f172a","#f8fafc","#e2e8f0"], ["EASY","Easy","#065f46","#f0fdf4","#bbf7d0"], ["MEDIUM","Medium","#78350f","#fffbeb","#fde68a"], ["HARD","Hard","#7f1d1d","#fff1f2","#fecaca"]].map(([val, label, activeText, activeBg, activeBorder]) => (
            <button key={val} onClick={() => setDiffFilter(val)}
              className="rounded-full px-3 py-1 text-xs font-bold transition border"
              style={diffFilter === val
                ? {color:activeText, background:activeBg, borderColor:activeBorder, boxShadow:"0 2px 8px rgba(0,0,0,0.1)", transform:"translateY(-1px)"}
                : {color:"#475569", background:"#ffffff", borderColor:"#e2e8f0"}
              }>
              {label}
            </button>
          ))}
        </div>

        {/* Topic filter pills — horizontal scroll */}
        <div className="flex gap-2 overflow-x-auto pb-1 scrollbar-thin">
          {TOPIC_FILTERS.map(t => (
            <button key={t} onClick={() => setTopicFilter(t)}
              className="flex-shrink-0 rounded-full px-3 py-1 text-xs font-semibold transition border"
              style={topicFilter === t
                ? {background:"#0f766e",color:"#ffffff",borderColor:"#0f766e",boxShadow:"0 2px 8px rgba(15,118,110,0.3)"}
                : {background:"#ffffff",color:"#334155",borderColor:"#e2e8f0"}
              }
              onMouseEnter={e => { if(topicFilter !== t){ e.target.style.borderColor="#0d9488"; e.target.style.color="#0f766e"; }}}
              onMouseLeave={e => { if(topicFilter !== t){ e.target.style.borderColor="#e2e8f0"; e.target.style.color="#334155"; }}}
            >
              {t}
            </button>
          ))}
        </div>

        <p className="text-xs font-medium" style={{color:"#64748b"}}>Showing {filtered.length} of {problems.length} problems</p>
      </div>

      {/* Error */}
      {error && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
          {error}
          <button onClick={loadProblems} className="ml-3 underline hover:no-underline">Retry</button>
        </div>
      )}

      {/* Loading */}
      {loading && (
        <div className="dashboard-card divide-y divide-slate-100">
          {[...Array(8)].map((_, i) => (
            <div key={i} className="flex animate-pulse items-center gap-4 px-5 py-4">
              <div className="h-4 w-6 rounded bg-slate-200" />
              <div className="h-4 flex-1 rounded bg-slate-200" />
              <div className="h-5 w-16 rounded-full bg-slate-200" />
              <div className="h-4 w-24 rounded bg-slate-200" />
              <div className="h-8 w-16 rounded-xl bg-slate-200" />
            </div>
          ))}
        </div>
      )}

      {/* Empty state */}
      {!loading && filtered.length === 0 && (
        <div className="dashboard-card p-12 text-center">
          <p className="text-4xl">🧩</p>
          <p className="mt-3 text-lg font-semibold text-slate-700">
            {problems.length === 0 ? "No problems available yet." : "No problems match your filters."}
          </p>
          {problems.length === 0 && (
            <p className="mt-1 text-sm text-slate-500">The backend will seed 15 problems on next startup.</p>
          )}
        </div>
      )}

      {/* Problem table */}
      {!loading && filtered.length > 0 && (
        <div className="dashboard-card overflow-hidden">
          {/* Table header */}
          <div className="grid grid-cols-[40px_1fr_100px_160px_80px] items-center gap-3 border-b border-slate-100 bg-slate-50 px-5 py-3">
            <span className="text-[11px] font-bold uppercase tracking-wider" style={{color:"#94a3b8"}}>#</span>
            <span className="text-[11px] font-bold uppercase tracking-wider" style={{color:"#94a3b8"}}>Title</span>
            <span className="text-[11px] font-bold uppercase tracking-wider" style={{color:"#94a3b8"}}>Difficulty</span>
            <span className="text-[11px] font-bold uppercase tracking-wider" style={{color:"#94a3b8"}}>Topics</span>
            <span></span>
          </div>

          {/* Table rows */}
          <div className="divide-y divide-slate-50">
            {filtered.map((problem, idx) => {
              const dm = diffMeta(problem.difficultyLevel);
              const tags = (problem.topicTags || "").split(",").map(t => t.trim()).filter(Boolean);
              return (
                <div
                  key={problem.id}
                  onClick={() => navigate(`/student/dsa/problem/${problem.id}`)}
                  className="grid grid-cols-[40px_1fr_100px_160px_80px] cursor-pointer items-center gap-3 px-5 py-4 transition hover:bg-teal-50/40 group"
                >
                  {/* Row number / solved status */}
                  <div className="flex items-center justify-center">
                    {problem.isSolved ? (
                      <span style={{color:"#10b981",fontSize:16}}>✓</span>
                    ) : (
                      <span className="text-sm" style={{color:"#94a3b8"}}>{idx + 1}</span>
                    )}
                  </div>

                  {/* Title */}
                  <div>
                    <p className="font-semibold text-sm transition" style={{color:"#0f172a"}}
                      onMouseEnter={e=>e.target.style.color="#0f766e"}
                      onMouseLeave={e=>e.target.style.color="#0f172a"}>
                      {problem.title}
                    </p>
                    {problem.isSolved && (
                      <p className="mt-0.5 text-xs font-medium" style={{color:"#10b981"}}>Solved ✓</p>
                    )}
                  </div>

                  {/* Difficulty badge */}
                  <div>
                    <span className="inline-block rounded-full border px-2.5 py-0.5 text-xs font-bold" style={
                      dm.label === "Easy"   ? {background:"#f0fdf4",color:"#065f46",borderColor:"#bbf7d0"} :
                      dm.label === "Medium" ? {background:"#fffbeb",color:"#78350f",borderColor:"#fde68a"} :
                      dm.label === "Hard"   ? {background:"#fff1f2",color:"#7f1d1d",borderColor:"#fecaca"} :
                                             {background:"#f8fafc",color:"#475569",borderColor:"#e2e8f0"}
                    }>
                      {dm.label}
                    </span>
                  </div>

                  {/* Topic tags */}
                  <div className="flex flex-wrap gap-1">
                    {tags.slice(0, 2).map(tag => (
                      <span key={tag} className="rounded-full px-2 py-0.5 text-[11px] font-medium"
                        style={{background:"#f1f5f9",color:"#475569"}}>
                        {tag}
                      </span>
                    ))}
                  </div>

                  {/* Solve button */}
                  <div>
                    <button
                      onClick={e => { e.stopPropagation(); navigate(`/student/dsa/problem/${problem.id}`); }}
                      className="rounded-xl px-3 py-1.5 text-xs font-bold text-white transition"
                      style={{background:"#0f766e"}}
                      onMouseEnter={e=>e.target.style.background="#0d9488"}
                      onMouseLeave={e=>e.target.style.background="#0f766e"}
                    >
                      {problem.isSolved ? "Review" : "Solve →"}
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};
