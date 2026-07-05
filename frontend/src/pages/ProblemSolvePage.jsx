import { useEffect, useState } from "react";
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
    } catch (e) {
      setError(e.response?.data?.message || "Failed to load problem.");
    } finally {
      setLoading(false);
    }
  };

  const loadSubmissions = async () => {
    try {
      const response = await api.get(`/api/coding/problems/${problemId}/submissions`);
      setSubmissions(Array.isArray(response.data) ? response.data : []);
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
    
    try {
      const response = await api.post("/api/coding/execute", {
        problemId: parseInt(problemId),
        language,
        code
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
            <textarea
              value={code}
              onChange={(e) => setCode(e.target.value)}
              spellCheck={false}
              placeholder="// Write your solution here..."
              style={{
                flex:1, resize:"none", border:"none", outline:"none",
                background:"#0d1117", color:"#e6edf3",
                padding:"16px", fontSize:13, lineHeight:1.65,
                fontFamily:"var(--font-mono)", caretColor:"#58a6ff",
                tabSize:2
              }}
            />
          </div>

          {/* Test Results */}
          {(testResults || error) && (
            <div className="max-h-64 overflow-y-auto border-t p-4" style={{background:"#161b22",borderColor:"#30363d"}}>
              <h3 className="mb-3 text-sm font-bold" style={{color:"#e6edf3"}}>Test Results</h3>

              {error && (
                <div className="rounded-lg p-3 text-sm" style={{background:"rgba(248,81,73,0.1)",border:"1px solid rgba(248,81,73,0.3)",color:"#ffa198"}}>
                  {error}
                </div>
              )}

              {testResults && (
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <span className="text-lg font-bold" style={{color: testResults.allTestsPassed ? "#3fb950" : "#f85149"}}>
                      {testResults.passedTests ?? testResults.testCasesPassed ?? 0}/{testResults.totalTests ?? testResults.testCasesTotal ?? 0} Tests Passed
                    </span>
                    {testResults.executionTime && (
                      <span className="text-xs" style={{color:"#8b949e"}}>⏱ {testResults.executionTime}ms</span>
                    )}
                  </div>

                  {testResults.testResults && testResults.testResults.map((test, index) => (
                    <div key={index} className="rounded-lg p-3"
                      style={{background: test.passed ? "rgba(63,185,80,0.08)" : "rgba(248,81,73,0.08)", border:`1px solid ${test.passed ? "rgba(63,185,80,0.2)" : "rgba(248,81,73,0.2)"}`}}>
                      <div className="flex items-center gap-2 mb-2">
                        <span style={{color: test.passed ? "#3fb950" : "#f85149", fontSize:14, fontWeight:700}}>
                          {test.passed ? "✓" : "✗"}
                        </span>
                        <span className="text-sm font-semibold" style={{color: test.passed ? "#3fb950" : "#f85149"}}>
                          Test Case {index + 1}
                        </span>
                      </div>
                      <div className="space-y-1 text-xs" style={{color:"#8b949e",fontFamily:"var(--font-mono)"}}>
                        {test.input && <p>Input: <span style={{color:"#e6edf3"}}>{test.input}</span></p>}
                        {test.expectedOutput && <p>Expected: <span style={{color:"#e6edf3"}}>{test.expectedOutput}</span></p>}
                        {test.actualOutput && <p>Got: <span style={{color:"#e6edf3"}}>{test.actualOutput}</span></p>}
                        {test.errorMessage && <p style={{color:"#ffa198"}}>Error: {test.errorMessage}</p>}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
