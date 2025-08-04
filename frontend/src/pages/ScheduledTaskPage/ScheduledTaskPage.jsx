// src/pages/ScheduledTasksPage/ScheduledTasksPage.jsx

import React, { useState, useEffect, useCallback } from 'react';
import { fetchTasks, toggleTaskActiveState, updateTask, deleteTask } from '../../api/taskApi'; // Import deleteTask
import { RotateCw, FileText, Loader2, AlertCircle } from 'lucide-react';

// ---- ÄNDRING 1: Byt ut importen ----
import ScheduledTasksTable from './components/ScheduledTasksTable';
import EditTaskModal from './components/EditTaskmodal';


function ScheduledTasksPage() {
    const [tasks, setTasks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [togglingTaskId, setTogglingTaskId] = useState(null);

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editingTask, setEditingTask] = useState(null);
    const [isUpdating, setIsUpdating] = useState(false);

    const loadTasks = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await fetchTasks();
            setTasks(data);
        } catch (e) {
            setError(e.message);
            console.error("Failed to fetch tasks:", e);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        loadTasks();
    }, [loadTasks]);

    const handleToggleActive = async (taskId) => {
        const originalTasks = [...tasks];
        setTogglingTaskId(taskId);

        // Optimistic update
        setTasks(currentTasks =>
            currentTasks.map(task =>
                task.id === taskId ? { ...task, active: !task.active } : task
            )
        );

        try {
            await toggleTaskActiveState(taskId);
        } catch (e) {
            console.error("Failed to toggle task status:", e);
            setError(`Failed to update task. Error: ${e.message || 'Unknown error'}. Please try again.`);
            setTasks(originalTasks); // Revert on error
        } finally {
            setTogglingTaskId(null);
        }
    };

    const handleOpenEditModal = (task) => {
        setEditingTask(task);
        setIsModalOpen(true);
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
        setEditingTask(null);
    };

    const handleUpdateTask = async (updatedData) => {
        if (!editingTask) return;
        
        setIsUpdating(true);
        setError(null);
        try {
            const updatedTask = await updateTask(editingTask.id, updatedData);
            // Update the task in the list state
            setTasks(currentTasks =>
                currentTasks.map(task =>
                    task.id === updatedTask.id ? updatedTask : task
                )
            );
            handleCloseModal(); // Close modal on success
        } catch (e) {
            setError(`Failed to update task: ${e.message}`);
            console.error("Failed to update task:", e);
        } finally {
            setIsUpdating(false);
        }
    };

    // New function to handle task deletion
    const handleDeleteTask = async (taskId) => {
        setError(null);
        try {
            await deleteTask(taskId);
            // Filter out the deleted task from the state
            setTasks(currentTasks => currentTasks.filter(task => task.id !== taskId));
            handleCloseModal(); // Close modal after successful deletion
        } catch (e) {
            setError(`Failed to delete task: ${e.message}`);
            console.error("Failed to delete task:", e);
        }
    };

    const renderContent = () => {
        if (loading) {
            // ... (denna del är oförändrad)
            return (
                <div className="min-h-[50vh] flex items-center justify-center">
                    <div className="text-center">
                        <Loader2 className="w-12 h-12 text-blue-600 animate-spin mx-auto mb-4" />
                        <p className="text-gray-600 dark:text-gray-400 text-lg">Loading your scheduled tasks...</p>
                    </div>
                </div>
            );
        }

        if (error && tasks.length === 0) {
            // ... (denna del är oförändrad)
            return (
                <div className="min-h-[50vh] flex items-center justify-center">
                    <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-8 max-w-md text-center">
                        <AlertCircle className="w-12 h-12 text-red-500 mx-auto mb-4" />
                        <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">Oops! Something went wrong</h2>
                        <p className="text-gray-600 dark:text-gray-400 mb-6">{error}</p>
                        <button
                            onClick={loadTasks}
                            className="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-6 rounded-lg transition-colors"
                        >
                            Try Again
                        </button>
                    </div>
                </div>
            );
        }

        // ---- ÄNDRING 2: Byt ut Card-gridden mot den nya Tabellen ----
        // Både "No tasks" och listan med tasks hanteras nu inuti ScheduledTasksTable.
        return (
            <ScheduledTasksTable
                tasks={tasks}
                onToggleActive={handleToggleActive}
                togglingTaskId={togglingTaskId}
                onEdit={handleOpenEditModal}
            />
        );
    };

    return (
        // ... (den yttre layouten är oförändrad) ...
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-950 dark:to-blue-950 font-sans">
            <div className="container mx-auto px-6 py-12 max-w-7xl">
                <header className="mb-12 flex flex-col sm:flex-row justify-between items-center sm:items-end">
                    <div>
                        <h1 className="text-4xl font-bold text-gray-900 dark:text-white mb-2 tracking-tight">My Scheduled Tasks</h1>
                        <p className="text-gray-600 dark:text-gray-400 text-lg">Automated tasks running for your agent.</p>
                    </div>
                    <button
                        onClick={loadTasks}
                        disabled={loading}
                        className="mt-6 sm:mt-0 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-blue-400 flex items-center gap-2 text-lg font-semibold transition-colors duration-200 shadow-md hover:shadow-lg"
                    >
                        <RotateCw className={`w-5 h-5 ${loading ? 'animate-spin' : ''}`} /> Refresh
                    </button>
                </header>

                {error && tasks.length > 0 && (
                    <div className="mb-8 p-4 bg-red-50 dark:bg-red-900/30 rounded-lg text-red-700 dark:text-red-300 text-center shadow-sm">
                        <AlertCircle className="w-5 h-5 inline-block mr-2" />
                        <span className="font-medium">Update failed:</span> {error}
                    </div>
                )}

                {renderContent()}
            </div>

            <EditTaskModal
                isOpen={isModalOpen}
                task={editingTask}
                onClose={handleCloseModal}
                onSave={handleUpdateTask}
                onDelete={handleDeleteTask} 
                isUpdating={isUpdating}
            />
        </div>
    );
}

export default ScheduledTasksPage;
