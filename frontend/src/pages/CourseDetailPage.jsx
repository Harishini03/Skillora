import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../lib/api";

export const CourseDetailPage = () => {
  const { courseId } = useParams();
  const navigate = useNavigate();
  const [course, setCourse] = useState(null);
  const [modules, setModules] = useState([]);
  const [currentLesson, setCurrentLesson] = useState(null);
  const [expandedModules, setExpandedModules] = useState({});
  const [isEnrolled, setIsEnrolled] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [completingLesson, setCompletingLesson] = useState(false);

  useEffect(() => {
    loadCourseData();
  }, [courseId]);

  const loadCourseData = async () => {
    setLoading(true);
    setError("");
    try {
      const [courseRes, modulesRes, enrollmentRes] = await Promise.all([
        api.get(`/api/courses/${courseId}`),
        api.get(`/api/courses/${courseId}/modules`),
        api.get(`/api/courses/${courseId}/enrollment`).catch(() => ({ data: null }))
      ]);

      setCourse(courseRes.data);
      setModules(Array.isArray(modulesRes.data) ? modulesRes.data : []);
      setIsEnrolled(!!enrollmentRes.data);

      // Auto-expand first module and load first lesson
      if (modulesRes.data && modulesRes.data.length > 0) {
        const firstModule = modulesRes.data[0];
        setExpandedModules({ [firstModule.id]: true });
        
        if (firstModule.lessons && firstModule.lessons.length > 0) {
          setCurrentLesson(firstModule.lessons[0]);
        }
      }
    } catch (e) {
      setError(e.response?.data?.message || "Failed to load course details.");
    } finally {
      setLoading(false);
    }
  };

  const enrollInCourse = async () => {
    setError("");
    try {
      await api.post(`/api/courses/${courseId}/enroll`);
      await loadCourseData();
    } catch (e) {
      setError(e.response?.data?.message || "Failed to enroll in course.");
    }
  };

  const toggleModule = (moduleId) => {
    setExpandedModules(prev => ({
      ...prev,
      [moduleId]: !prev[moduleId]
    }));
  };

  const selectLesson = (lesson) => {
    setCurrentLesson(lesson);
  };

  const markLessonComplete = async (lessonId) => {
    if (!isEnrolled) return;
    
    setCompletingLesson(true);
    setError("");
    try {
      await api.post(`/api/courses/lessons/${lessonId}/complete`);
      await loadCourseData(); // Reload to update progress
    } catch (e) {
      setError(e.response?.data?.message || "Failed to mark lesson as complete.");
    } finally {
      setCompletingLesson(false);
    }
  };

  const renderLessonContent = () => {
    if (!currentLesson) {
      return (
        <div className="flex h-full items-center justify-center p-12 text-center">
          <div>
            <svg className="mx-auto h-16 w-16 text-slate-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
            </svg>
            <p className="mt-4 text-slate-600">Select a lesson to start learning</p>
          </div>
        </div>
      );
    }

    return (
      <div className="h-full overflow-y-auto">
        <div className="sticky top-0 z-10 border-b border-slate-200 bg-white p-6">
          <h2 className="text-2xl font-bold text-slate-900">{currentLesson.title}</h2>
          <div className="mt-2 flex items-center gap-4">
            <span className="rounded-full bg-teal-100 px-3 py-1 text-xs font-semibold text-teal-800">
              {currentLesson.lessonType || "READING"}
            </span>
            {currentLesson.duration && (
              <span className="text-sm text-slate-500">
                <svg className="inline h-4 w-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                {currentLesson.duration} min
              </span>
            )}
            {currentLesson.isCompleted && (
              <span className="text-sm font-semibold text-emerald-600">
                <svg className="inline h-4 w-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                </svg>
                Completed
              </span>
            )}
          </div>
        </div>

        <div className="p-6">
          {currentLesson.lessonType === "VIDEO" && currentLesson.videoUrl && (
            <div className="mb-6 aspect-video w-full overflow-hidden rounded-xl bg-slate-900">
              <iframe
                src={currentLesson.videoUrl}
                className="h-full w-full"
                allowFullScreen
                title={currentLesson.title}
              />
            </div>
          )}

          {currentLesson.content && (
            <div className="prose prose-slate max-w-none">
              <div dangerouslySetInnerHTML={{ __html: currentLesson.content }} />
            </div>
          )}

          {!currentLesson.content && !currentLesson.videoUrl && (
            <div className="rounded-xl border border-slate-200 bg-slate-50 p-8 text-center">
              <p className="text-slate-600">Content for this lesson is being prepared.</p>
            </div>
          )}

          {isEnrolled && !currentLesson.isCompleted && (
            <div className="mt-8 flex justify-end">
              <button
                onClick={() => markLessonComplete(currentLesson.id)}
                disabled={completingLesson}
                className="rounded-xl bg-teal-700 px-6 py-3 text-sm font-semibold text-white hover:bg-teal-600 transition disabled:opacity-60"
              >
                {completingLesson ? "Marking Complete..." : "Mark as Complete"}
              </button>
            </div>
          )}
        </div>
      </div>
    );
  };

  if (loading) return <div className="p-6">Loading course...</div>;
  if (error && !course) return <div className="p-6 text-rose-600">{error}</div>;

  return (
    <div className="flex h-[calc(100vh-4rem)] flex-col">
      {/* Course Header */}
      <div className="border-b border-slate-200 bg-gradient-to-r from-teal-600 to-cyan-600 p-6 text-white">
        <button
          onClick={() => navigate("/student/courses")}
          className="mb-4 flex items-center gap-2 text-sm text-cyan-100 hover:text-white transition"
        >
          <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          Back to Courses
        </button>

        <div className="flex items-start justify-between">
          <div className="flex-1">
            <h1 className="text-3xl font-bold">{course?.title}</h1>
            <p className="mt-2 text-cyan-100">{course?.description}</p>
            {course?.progress > 0 && (
              <div className="mt-4 max-w-md">
                <div className="flex items-center justify-between mb-1">
                  <span className="text-sm font-medium">Your Progress</span>
                  <span className="text-sm font-semibold">{course.progress}%</span>
                </div>
                <div className="h-2 rounded-full bg-cyan-900/30 overflow-hidden">
                  <div 
                    className="h-full bg-white transition-all duration-300"
                    style={{ width: `${course.progress}%` }}
                  />
                </div>
              </div>
            )}
          </div>

          {!isEnrolled && (
            <button
              onClick={enrollInCourse}
              className="rounded-xl bg-white px-6 py-3 font-semibold text-teal-700 hover:bg-cyan-50 transition"
            >
              Enroll in Course
            </button>
          )}
        </div>
      </div>

      {error && <div className="bg-rose-50 border-b border-rose-200 p-3 text-sm text-rose-700">{error}</div>}

      {/* Main Content Area */}
      <div className="flex flex-1 overflow-hidden">
        {/* Sidebar - Curriculum */}
        <div className="w-80 overflow-y-auto border-r border-slate-200 bg-slate-50">
          <div className="p-4">
            <h3 className="mb-4 text-sm font-bold uppercase tracking-wide text-slate-700">
              Course Curriculum
            </h3>

            <div className="space-y-2">
              {modules.length === 0 && (
                <p className="text-sm text-slate-600">No modules available yet.</p>
              )}

              {modules.map((module, moduleIndex) => (
                <div key={module.id} className="overflow-hidden rounded-xl border border-slate-200 bg-white">
                  <button
                    onClick={() => toggleModule(module.id)}
                    className="flex w-full items-center justify-between p-4 text-left hover:bg-slate-50 transition"
                  >
                    <div className="flex-1">
                      <div className="flex items-center gap-2">
                        <span className="flex h-6 w-6 items-center justify-center rounded-full bg-teal-100 text-xs font-bold text-teal-700">
                          {moduleIndex + 1}
                        </span>
                        <span className="font-semibold text-slate-900">{module.title}</span>
                      </div>
                      <p className="mt-1 text-xs text-slate-500">
                        {module.lessons?.length || 0} lessons
                      </p>
                    </div>
                    <svg
                      className={`h-5 w-5 text-slate-400 transition-transform ${
                        expandedModules[module.id] ? "rotate-180" : ""
                      }`}
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                    </svg>
                  </button>

                  {expandedModules[module.id] && module.lessons && (
                    <div className="border-t border-slate-200">
                      {module.lessons.map((lesson, lessonIndex) => (
                        <button
                          key={lesson.id}
                          onClick={() => selectLesson(lesson)}
                          className={`flex w-full items-center gap-3 border-b border-slate-100 p-3 text-left text-sm hover:bg-teal-50 transition last:border-b-0 ${
                            currentLesson?.id === lesson.id ? "bg-teal-50" : ""
                          }`}
                        >
                          <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-lg bg-slate-100">
                            {lesson.isCompleted ? (
                              <svg className="h-5 w-5 text-emerald-600" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                              </svg>
                            ) : lesson.lessonType === "VIDEO" ? (
                              <svg className="h-4 w-4 text-slate-600" fill="currentColor" viewBox="0 0 20 20">
                                <path d="M10 18a8 8 0 100-16 8 8 0 000 16zM9.555 7.168A1 1 0 008 8v4a1 1 0 001.555.832l3-2a1 1 0 000-1.664l-3-2z" />
                              </svg>
                            ) : lesson.lessonType === "QUIZ" ? (
                              <svg className="h-4 w-4 text-slate-600" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
                              </svg>
                            ) : (
                              <svg className="h-4 w-4 text-slate-600" fill="currentColor" viewBox="0 0 20 20">
                                <path d="M9 4.804A7.968 7.968 0 005.5 4c-1.255 0-2.443.29-3.5.804v10A7.969 7.969 0 015.5 14c1.669 0 3.218.51 4.5 1.385A7.962 7.962 0 0114.5 14c1.255 0 2.443.29 3.5.804v-10A7.968 7.968 0 0014.5 4c-1.255 0-2.443.29-3.5.804V12a1 1 0 11-2 0V4.804z" />
                              </svg>
                            )}
                          </div>
                          <div className="flex-1 min-w-0">
                            <p className={`truncate ${currentLesson?.id === lesson.id ? "font-semibold text-teal-900" : "text-slate-700"}`}>
                              {lesson.title}
                            </p>
                            {lesson.duration && (
                              <p className="text-xs text-slate-500">{lesson.duration} min</p>
                            )}
                          </div>
                        </button>
                      ))}
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Main Content Area */}
        <div className="flex-1 overflow-hidden bg-white">
          {renderLessonContent()}
        </div>
      </div>
    </div>
  );
};
