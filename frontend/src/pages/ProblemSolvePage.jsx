import { useEffect, useState, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../lib/api";

const CODE_TEMPLATES = {
  JAVA: `public class Solution {
    public static void main(String[] args) {
        // Your code here
    }
}`,
  PYTHON: `# Your code here
def solution():
    pass

if __name__ == "__main__":
    solution()`,
  JAVASCRIPT: `// Your code here
function solution() {
    
}

solution();`,
  CPP: `#include <iostream>
using namespace std;

int main() {
    // Your code here
    return 0;
}`
};

export const ProblemSolvePage = () => {
  const { problemId } = useParams();
  const navigate = useNavigate();
  const [problem, setProblem] = useState(null);
  const [language, setLanguage] = useState("PYTHON");
  const [code, setCode] = useState(CODE_TEMPLATES.PYTHON);
  const [testResults, setTestResults] = useState(null);
  const [submissions, setSubmissions] = useState([]);
  const [activeTab, setActiveTab] = useState("description"); // description | submissions
  const [loading, setLoading] = useState(true);
  const [running, setRunning] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  const textareaRef = useRef(null);
  const gutterRef = useRef(null);
  const [consoleExpanded, setConsoleExpanded] = useState(false);
  const [consoleTab, setConsoleTab] = useState("testcases"); // testcases | results
  const [customInput, setCustomInput] = useState("");

  const handleTextareaScroll = () => {
    if (textareaRef.current && gutterRef.current) {
      gutterRef.current.scrollTop = textareaRef.current.scrollTop;
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === "Tab") {
      e.preventDefault();
      const start = e.target.selectionStart;
      const end = e.target.selectionEnd;
      const val = e.target.value;
      const nextCode = val.substring(0, start) + "    " + val.substring(end);
      setCode(nextCode);
      setTimeout(() => {
        if (textareaRef.current) {
          textareaRef.current.selectionStart = textareaRef.current.selectionEnd = start + 4;
        }
      }, 0);
    }
    const brackets = {
      "(": ")",
      "[": "]",
      "{": "}",
      '"': '"',
      "'": "'",
      "`": "`"
    };
    if (brackets[e.key] !== undefined) {
      e.preventDefault();
      const start = e.target.selectionStart;
      const end = e.target.selectionEnd;
      const val = e.target.value;
      const closing = brackets[e.key];
      const nextCode = val.substring(0, start) + e.key + closing + val.substring(end);
      setCode(nextCode);
      setTimeout(() => {
        if (textareaRef.current) {
          textareaRef.current.selectionStart = textareaRef.current.selectionEnd = start + 1;
        }
      }, 0);
    }
  };

  useEffect(() => {
    loadProblem();
    loadSubmissions();
  }, [problemId]);

  const loadProblem = async () => {
    setLoading(true);
    setError("");
    try {
      const response = await api.get(`/api/coding/problems/${problemId}`);
      setProblem(response.data);
      if (response.data?.examples?.length > 0) {
        setCustomInput(response.data.examples[0].input || "");
      }
    } catch (e) {
      setError(e.response?.data?.message || "Failed to load problem.");
    } finally {
      setLoading(false);
    }
  };

  const loadSubmissions = async () => {
    try {
      const response = await api.get("/api/coding/submissions");
      // Filter to only this problem's submissions
      const all = Array.isArray(response.data) ? response.data : [];
      setSubmissions(all.filter(s => String(s.problemId) === String(problemId)));
    } catch (e) {
      // Ignore submission loading errors
    }
  };

  const handleLanguageChange = (newLanguage) => {
    setLanguage(newLanguage);
    setCode(CODE_TEMPLATES[newLanguage]);
    setTestResults(null);
  };

  const runCode = async () => {
    setRunning(true);
    setError("");
    setTestResults(null);
    setConsoleExpanded(true); // Open the drawer immediately when running
    setConsoleTab("results");
    
    try {
      const response = await api.post("/api/coding/execute", {
        problemId: parseInt(problemId),
        language,
        code,
        customInput
      });

      setTestResults(response.data);
    } catch (e) {
      setError(e.response?.data?.message || "Failed to run code.");
    } finally {
      setRunning(false);
    }
  };

  const submitSolution = async () => {
    setSubmitting(true);
    setError("");
    setConsoleExpanded(true); // Open immediately on submit
    setConsoleTab("results");
    
    try {
      const response = await api.post("/api/coding/submit", {
        problemId: parseInt(problemId),
        language,
        code
      });

      setTestResults(response.data);
      await loadSubmissions();
    } catch (e) {
      setError(e.response?.data?.message || "Failed to submit solution.");
    } finally {
      setSubmitting(false);
    }
  };

  const getDifficultyColor = (difficulty) => {
    switch (difficulty) {
      case "EASY":
        return "bg-emerald-100 text-emerald-800";
      case "MEDIUM":
        return "bg-amber-100 text-amber-800";
      case "HARD":
        return "bg-rose-100 text-rose-800";
      default:
        return "bg-slate-100 text-slate-800";
    }
  };

  if (loading) return <div className="p-6">Loading problem...</div>;
  if (!problem) return <div className="p-6 text-rose-600">Problem not found.</div>;

  return (
    <div className="flex h-[calc(100vh-4rem)] flex-col">
      {/* Header */}
      <div className="border-b border-slate-200 bg-white p-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <button
              onClick={() => navigate("/student/dsa")}
              className="flex items-center gap-2 text-sm text-slate-600 hover:text-slate-900"
            >
              <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
              Back to Problems
            </button>
            <div className="h-6 w-px bg-slate-300" />
            <h1 className="text-xl font-bold text-slate-900">{problem.title}</h1>
            <span className={`rounded-full px-3 py-1 text-xs font-semibold ${getDifficultyColor(problem.difficulty)}`}>
              {problem.difficulty}
            </span>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex flex-1 overflow-hidden">
        {/* Left Panel - Problem Description */}
        <div className="w-1/2 overflow-y-auto border-r border-slate-200 bg-white">
          <div className="p-6">
            {/* Tabs */}
            <div className="flex gap-2 border-b border-slate-200 mb-4">
              <button
                onClick={() => setActiveTab("description")}
                className={`px-4 py-2 text-sm font-semibold transition ${
                  activeTab === "description"
                    ? "border-b-2 border-teal-600 text-teal-700"
                    : "text-slate-600 hover:text-slate-900"
                }`}
              >
                Description
              </button>
              <button
                onClick={() => setActiveTab("submissions")}
                className={`px-4 py-2 text-sm font-semibold transition ${
                  activeTab === "submissions"
                    ? "border-b-2 border-teal-600 text-teal-700"
                    : "text-slate-600 hover:text-slate-900"
                }`}
              >
                Submissions ({submissions.length})
              </button>
            </div>

            {/* Description Tab */}
            {activeTab === "description" && (
              <div className="space-y-6">
                <div>
                  <h2 className="text-lg font-bold mb-3" style={{color:"#0f172a"}}>Problem Description</h2>
                  <div className="text-sm leading-relaxed whitespace-pre-wrap" style={{color:"#334155"}}>
                    {problem.description || "No description available."}
                  </div>
                </div>

                {problem.inputFormat && (
                  <div>
                    <h3 className="text-md font-bold text-slate-900 mb-2">Input Format</h3>
                    <div className="rounded-lg bg-slate-50 p-4 font-mono text-sm text-slate-700">
                      {problem.inputFormat}
                    </div>
                  </div>
                )}

                {problem.outputFormat && (
                  <div>
                    <h3 className="text-md font-bold text-slate-900 mb-2">Output Format</h3>
                    <div className="rounded-lg bg-slate-50 p-4 font-mono text-sm text-slate-700">
                      {problem.outputFormat}
                    </div>
                  </div>
                )}

                {problem.constraints && (
                  <div>
                    <h3 className="text-md font-bold text-slate-900 mb-2">Constraints</h3>
                    <div className="rounded-lg bg-slate-50 p-4 font-mono text-sm text-slate-700">
                      {problem.constraints}
                    </div>
                  </div>
                )}

                {problem.examples && problem.examples.length > 0 && (
                  <div>
                    <h3 className="text-md font-bold text-slate-900 mb-2">Examples</h3>
                    <div className="space-y-4">
                      {problem.examples.map((example, index) => (
                        <div key={index} className="rounded-lg border border-slate-200 p-4">
                          <p className="text-sm font-semibold text-slate-900 mb-2">Example {index + 1}</p>
                          <div className="space-y-2">
                            <div>
                              <p className="text-xs font-semibold text-slate-600 mb-1">Input:</p>
                              <pre className="rounded bg-slate-50 p-2 text-xs text-slate-700">{example.input}</pre>
                            </div>
                            <div>
                              <p className="text-xs font-semibold text-slate-600 mb-1">Output:</p>
                              <pre className="rounded bg-slate-50 p-2 text-xs text-slate-700">{example.output}</pre>
                            </div>
                            {example.explanation && (
                              <div>
                                <p className="text-xs font-semibold text-slate-600 mb-1">Explanation:</p>
                                <p className="text-xs text-slate-600">{example.explanation}</p>
                              </div>
                            )}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            )}

            {/* Submissions Tab */}
            {activeTab === "submissions" && (
              <div className="space-y-3">
                {submissions.length === 0 && (
                  <p className="text-center text-slate-600 py-8">No submissions yet. Start coding!</p>
                )}
                {submissions.map((submission) => (
                  <div key={submission.id} className="rounded-lg border border-slate-200 p-4">
                    <div className="flex items-center justify-between mb-2">
                      <span className={`rounded-full px-3 py-1 text-xs font-semibold ${
                        submission.status === "ACCEPTED" ? "bg-emerald-100 text-emerald-800" : "bg-rose-100 text-rose-800"
                      }`}>
                        {submission.status}
                      </span>
                      <span className="text-xs text-slate-500">
                        {new Date(submission.submittedAt).toLocaleString()}
                      </span>
                    </div>
                    <div className="flex items-center gap-4 text-sm text-slate-600">
                      <span>{submission.language}</span>
                      <span>•</span>
                      <span>{submission.passedTests}/{submission.totalTests} tests passed</span>
                      {submission.executionTime && (
                        <>
                          <span>•</span>
                          <span>{submission.executionTime}ms</span>
                        </>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Right Panel - Code Editor */}
        <div className="flex w-1/2 flex-col" style={{background:"#0d1117"}}>
          {/* Editor Header */}
          <div className="flex items-center justify-between border-b px-4 py-3" style={{background:"#161b22",borderColor:"#30363d"}}>
            <select
              value={language}
              onChange={(e) => handleLanguageChange(e.target.value)}
              style={{background:"#21262d",border:"1px solid #30363d",color:"#e6edf3",borderRadius:8,padding:"5px 12px",fontSize:13,fontFamily:"var(--font-mono)",outline:"none"}}
            >
              <option value="PYTHON">🐍 Python</option>
              <option value="JAVA">☕ Java</option>
              <option value="JAVASCRIPT">🟨 JavaScript</option>
              <option value="CPP">⚙️ C++</option>
            </select>

            <div className="flex gap-2">
              <button
                onClick={runCode}
                disabled={running || submitting}
                style={{background:"#21262d",border:"1px solid #30363d",color:"#e6edf3",borderRadius:8,padding:"5px 14px",fontSize:13,fontWeight:600,cursor:"pointer",opacity:running||submitting?0.5:1}}
                onMouseEnter={e=>e.target.style.background="#2d333b"}
                onMouseLeave={e=>e.target.style.background="#21262d"}
              >
                {running ? "⏳ Running..." : "▶ Run Code"}
              </button>
              <button
                onClick={submitSolution}
                disabled={running || submitting}
                style={{background:"#238636",border:"1px solid #2ea043",color:"#ffffff",borderRadius:8,padding:"5px 14px",fontSize:13,fontWeight:600,cursor:"pointer",opacity:running||submitting?0.5:1}}
                onMouseEnter={e=>e.target.style.background="#2ea043"}
                onMouseLeave={e=>e.target.style.background="#238636"}
              >
                {submitting ? "⏳ Submitting..." : "✓ Submit"}
              </button>
            </div>
          </div>

          {/* Code Editor — line numbers + textarea */}
          <div className="flex flex-1 overflow-hidden" style={{fontFamily:"var(--font-mono)"}}>
            {/* Gutter */}
            <div
              ref={gutterRef}
              style={{
                width: "48px",
                background: "#0d1117",
                color: "#565c64",
                padding: "16px 8px 16px 0",
                textAlign: "right",
                userSelect: "none",
                borderRight: "1px solid #21262d",
                fontFamily: "var(--font-mono)",
                fontSize: "13px",
                lineHeight: "22px",
                overflow: "hidden",
              }}
            >
              {Array.from({ length: code.split("\n").length || 1 }, (_, i) => i + 1).map(n => (
                <div key={n} style={{ height: "22px" }}>{n}</div>
              ))}
            </div>

            {/* Textarea */}
            <textarea
              ref={textareaRef}
              value={code}
              onChange={(e) => setCode(e.target.value)}
              onScroll={handleTextareaScroll}
              onKeyDown={handleKeyDown}
              spellCheck={false}
              placeholder="// Write your solution here..."
              style={{
                flex: 1,
                resize: "none",
                border: "none",
                outline: "none",
                background: "#0d1117",
                color: "#e6edf3",
                padding: "16px",
                fontSize: "13px",
                lineHeight: "22px",
                fontFamily: "var(--font-mono)",
                caretColor: "#58a6ff",
                tabSize: 4,
                whiteSpace: "pre",
                overflow: "auto"
              }}
            />
          </div>

          {/* LeetCode Collapsible Console Drawer */}
          <div className="flex flex-col border-t" style={{background:"#161b22",borderColor:"#30363d"}}>
            {/* Console Header / Toggle Bar */}
            <div className="flex items-center justify-between px-4 py-2.5 bg-[#161b22] select-none transition">
              <div className="flex items-center gap-4">
                <button
                  onClick={() => setConsoleExpanded(!consoleExpanded)}
                  className="flex items-center gap-2 hover:text-white transition"
                  style={{color: consoleExpanded ? "#e6edf3" : "#8b949e", fontSize: 13, fontWeight: "bold"}}
                >
                  Console {consoleExpanded ? "▼" : "▲"}
                </button>
                {consoleExpanded && (
                  <div className="flex gap-4 border-l pl-4 border-[#30363d]">
                    <button onClick={() => setConsoleTab("testcases")} className="text-[13px] font-semibold hover:text-white transition" style={{color: consoleTab === "testcases" ? "#e6edf3" : "#8b949e", borderBottom: consoleTab === "testcases" ? "2px solid #e6edf3" : "2px solid transparent"}}>Testcase</button>
                    <button onClick={() => setConsoleTab("results")} className="text-[13px] font-semibold hover:text-white transition" style={{color: consoleTab === "results" ? "#e6edf3" : "#8b949e", borderBottom: consoleTab === "results" ? "2px solid #e6edf3" : "2px solid transparent"}}>Test Result</button>
                  </div>
                )}
              </div>
              <div className="flex items-center gap-2">
                {testResults && !consoleExpanded && (
                  <span
                    className="rounded px-2 py-0.5 text-xs font-bold"
                    style={{
                      background: testResults.allTestsPassed ? "rgba(63,185,80,0.1)" : "rgba(248,81,73,0.1)",
                      color: testResults.allTestsPassed ? "#3fb950" : "#f85149"
                    }}
                  >
                    {testResults.allTestsPassed ? "All Passed" : "Failed"}
                  </span>
                )}
              </div>
            </div>

            {/* Console Drawer Content */}
            {consoleExpanded && (
              <div className="overflow-y-auto p-4 border-t flex flex-col" style={{height:"240px", borderColor:"#30363d", background:"#0d1117"}}>
                {consoleTab === "testcases" && (
                  <div className="flex flex-col flex-1 h-full">
                    <p className="text-xs mb-2 font-semibold" style={{color:"#8b949e"}}>Custom Input (Newline separated parameters)</p>
                    <textarea
                      value={customInput}
                      onChange={(e) => setCustomInput(e.target.value)}
                      spellCheck={false}
                      placeholder="Enter custom test cases here..."
                      style={{
                        flex: 1,
                        background: "#161b22",
                        border: "1px solid #30363d",
                        color: "#e6edf3",
                        borderRadius: "6px",
                        padding: "12px",
                        fontFamily: "var(--font-mono)",
                        fontSize: "12px",
                        outline: "none",
                        resize: "none"
                      }}
                    />
                  </div>
                )}
                
                {consoleTab === "results" && (
                  <div className="w-full">
                    {!(testResults || error) && (
                      <div className="text-sm py-4 text-center" style={{color:"#8b949e"}}>
                        Run or Submit code to see test results here.
                      </div>
                    )}

                    {error && (
                      <div className="rounded-lg p-3 text-sm" style={{background:"rgba(248,81,73,0.1)",border:"1px solid rgba(248,81,73,0.3)",color:"#ffa198"}}>
                        {error}
                      </div>
                    )}

                {testResults && (
                  <div className="space-y-4">
                    <div className="flex items-center justify-between pb-2 border-b" style={{borderColor:"#21262d"}}>
                      <span className="text-base font-bold" style={{color: testResults.allTestsPassed ? "#3fb950" : "#f85149"}}>
                        {testResults.passedTests ?? testResults.testCasesPassed ?? 0}/{testResults.totalTests ?? testResults.testCasesTotal ?? 0} Test Cases Passed
                      </span>
                      {testResults.executionTime && (
                        <span className="text-xs" style={{color:"#8b949e"}}>⏱ Execution Time: {testResults.executionTime}ms</span>
                      )}
                    </div>

                    {testResults.testResults && testResults.testResults.map((test, index) => (
                      <div key={index} className="rounded-lg p-3"
                        style={{background: test.passed ? "rgba(63,185,80,0.04)" : "rgba(248,81,73,0.04)", border:`1px solid ${test.passed ? "rgba(63,185,80,0.15)" : "rgba(248,81,73,0.15)"}`}}>
                        <div className="flex items-center gap-2 mb-2">
                          <span style={{color: test.passed ? "#3fb950" : "#f85149", fontSize:14, fontWeight:700}}>
                            {test.passed ? "✓" : "✗"}
                          </span>
                          <span className="text-sm font-semibold" style={{color: test.passed ? "#3fb950" : "#f85149"}}>
                            Case {index + 1}
                          </span>
                        </div>
                        <div className="space-y-1 text-xs" style={{color:"#8b949e",fontFamily:"var(--font-mono)"}}>
                          {test.input && <p>Input: <span style={{color:"#e6edf3"}}>{test.input}</span></p>}
                          {test.expectedOutput && <p>Expected: <span style={{color:"#e6edf3"}}>{test.expectedOutput}</span></p>}
                          {test.actualOutput && <p>Actual: <span style={{color:"#e6edf3"}}>{test.actualOutput}</span></p>}
                          {test.errorMessage && <p style={{color:"#ffa198"}}>Error: {test.errorMessage}</p>}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
                </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
