import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../lib/api";

export const CoursesPage = () => {
  const navigate = useNavigate();
  const [courses, setCourses] = useState([]);
  const [enrolledCourses, setEnrolledCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [activeTab, setActiveTab] = useState("available"); // available | enrolled

  useEffect(() => {
    loadCourses();
  }, []);

  const loadCourses = async () => {
    setLoading(true);
    setError("");
    try {
      const [allCoursesRes, enrolledRes] = await Promise.all([
        api.get("/api/courses"),
        api.get("/api/courses/enrolled").catch(() => ({ data: [] }))
      ]);
      setCourses(Array.isArray(allCoursesRes.data) ? allCoursesRes.data : []);
      setEnrolledCourses(Array.isArray(enrolledRes.data) ? enrolledRes.data : []);
    } catch (e) {
      setError(e.response?.data?.message || "Failed to load courses.");
    } finally {
      setLoading(false);
    }
  };

  const enrollInCourse = async (courseId) => {
    setError("");
    try {
      await api.post(`/api/courses/${courseId}/enroll`);
      await loadCourses();
    } catch (e) {
      setError(e.response?.data?.message || "Failed to enroll in course.");
    }
  };

  const isEnrolled = (courseId) => {
    return enrolledCourses.some(ec => ec.id === courseId);
  };

  if (loading) return <div className="p-6">Loading courses...</div>;

  const displayedCourses = activeTab === "available" ? courses : enrolledCourses;

  return (
    <div className="space-y-6">
      <section className="portal-banner">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-100">Learning Portal</p>
        <h2 className="mt-2 text-3xl font-bold text-white">Courses & Learning Paths</h2>
        <p className="mt-2 text-sm text-cyan-50/90">
          Master placement readiness through structured courses with videos, quizzes, and hands-on practice.
        </p>
      </section>

      <div className="flex gap-2 border-b border-slate-200">
        <button
          onClick={() => setActiveTab("available")}
          className={`px-4 py-2 text-sm font-semibold transition ${
            activeTab === "available"
              ? "border-b-2 border-teal-600 text-teal-700"
              : "text-slate-600 hover:text-slate-900"
          }`}
        >
          All Courses ({courses.length})
        </button>
        <button
          onClick={() => setActiveTab("enrolled")}
          className={`px-4 py-2 text-sm font-semibold transition ${
            activeTab === "enrolled"
              ? "border-b-2 border-teal-600 text-teal-700"
              : "text-slate-600 hover:text-slate-900"
          }`}
        >
          My Courses ({enrolledCourses.length})
        </button>
      </div>

      {error && <p className="text-sm text-rose-600">{error}</p>}

      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        {displayedCourses.length === 0 && (
          <div className="col-span-full dashboard-card p-8 text-center">
            <p className="text-slate-600">
              {activeTab === "available" 
                ? "No courses available yet." 
                : "You haven't enrolled in any courses yet."}
            </p>
          </div>
        )}

        {displayedCourses.map((course) => {
          const enrolled = isEnrolled(course.id);
          const progress = course.progress || 0;

          return (
            <div key={course.id} className="dashboard-card flex flex-col overflow-hidden">
              {course.thumbnailUrl && (
                <div className="h-40 w-full overflow-hidden bg-gradient-to-br from-teal-500 to-cyan-600">
                  <img 
                    src={course.thumbnailUrl} 
                    alt={course.title}
                    className="h-full w-full object-cover"
                    onError={(e) => e.target.style.display = 'none'}
                  />
                </div>
              )}
              {!course.thumbnailUrl && (
                <div className="h-40 w-full bg-gradient-to-br from-teal-500 to-cyan-600 flex items-center justify-center">
                  <svg className="h-16 w-16 text-white opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
                  </svg>
                </div>
              )}

              <div className="flex flex-1 flex-col p-5">
                <div className="mb-2 flex items-center gap-2">
                  <span className="rounded-full bg-teal-100 px-2 py-1 text-xs font-semibold text-teal-800">
                    {course.level || "All Levels"}
                  </span>
                  <span className="text-xs text-slate-500">
                    {course.duration || "Self-paced"}
                  </span>
                </div>

                <h3 className="mb-2 text-lg font-bold text-slate-900">{course.title}</h3>
                <p className="mb-4 flex-1 text-sm text-slate-600 line-clamp-3">
                  {course.description}
                </p>

                {enrolled && progress > 0 && (
                  <div className="mb-3">
                    <div className="flex items-center justify-between mb-1">
                      <span className="text-xs font-medium text-slate-700">Progress</span>
                      <span className="text-xs font-semibold text-teal-700">{progress}%</span>
                    </div>
                    <div className="h-2 rounded-full bg-slate-200 overflow-hidden">
                      <div 
                        className="h-full bg-gradient-to-r from-teal-500 to-cyan-500 transition-all duration-300"
                        style={{ width: `${progress}%` }}
                      />
                    </div>
                  </div>
                )}

                <div className="flex items-center gap-3">
                  {enrolled ? (
                    <button
                      onClick={() => navigate(`/student/course/${course.id}`)}
                      className="flex-1 rounded-xl bg-teal-700 px-4 py-2 text-sm font-semibold text-white hover:bg-teal-600 transition"
                    >
                      {progress > 0 ? "Continue Learning" : "Start Course"}
                    </button>
                  ) : (
                    <>
                      <button
                        onClick={() => enrollInCourse(course.id)}
                        className="flex-1 rounded-xl bg-teal-700 px-4 py-2 text-sm font-semibold text-white hover:bg-teal-600 transition"
                      >
                        Enroll Now
                      </button>
                      <button
                        onClick={() => navigate(`/student/course/${course.id}`)}
                        className="rounded-xl border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50 transition"
                      >
                        Preview
                      </button>
                    </>
                  )}
                </div>

                {course.instructor && (
                  <div className="mt-3 flex items-center gap-2 text-xs text-slate-500">
                    <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                    <span>{course.instructor}</span>
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
