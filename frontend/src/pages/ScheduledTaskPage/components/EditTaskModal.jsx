import React, { useState, useEffect } from 'react';
import { X, Trash2 } from 'lucide-react'; // Import Trash2 icon for delete button

const EditTaskModal = ({ task, isOpen, onClose, onSave, isUpdating, onDelete }) => {
    // State for the form inputs
    const [prompt, setPrompt] = useState('');
    const [interval, setInterval] = useState(4);
    const [unit, setUnit] = useState('hours');
    const [isDeleting, setIsDeleting] = useState(false); // New state for delete loading

    useEffect(() => {
        if (task) {
            setPrompt(task.prompt);
            // Assuming task.humanReadableInterval and task.humanReadableUnit exist for pre-filling
            // If they don't, you might need to parse the cron expression to get the values.
            // For now, we'll set defaults.
            setInterval(4);
            setUnit('hours');
        }
    }, [task]);

    if (!isOpen) {
        return null;
    }

    const handleSubmit = (e) => {
        e.preventDefault();
        const payload = {
            prompt,
            interval: parseInt(interval, 10),
            unit: unit
        };
        onSave(payload);
    };

    const handleDelete = async () => {
        // Instead of window.confirm, you'd typically use a custom modal for confirmation
        // For this example, we'll use a simple alert for brevity.
        // IMPORTANT: In a real application, replace this with a proper UI modal.
        const confirmDelete = window.confirm("Are you sure you want to delete this task? This action cannot be undone.");
        if (confirmDelete) {
            setIsDeleting(true);
            try {
                await onDelete(task.id); // Call the onDelete prop with the task ID
                onClose(); // Close the modal after successful deletion
            } catch (error) {
                console.error("Failed to delete task:", error);
                // You might want to show an error message to the user here
            } finally {
                setIsDeleting(false);
            }
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-60 backdrop-blur-sm">
            <div className="relative w-full max-w-2xl p-8 bg-white dark:bg-gray-800 rounded-2xl shadow-xl m-4">
                <button onClick={onClose} className="absolute top-4 right-4 text-gray-400 hover:text-gray-600" aria-label="Close">
                    <X className="w-6 h-6" />
                </button>
                <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-6">Edit Scheduled Task</h2>
                <form onSubmit={handleSubmit} className="space-y-6">
                    <div>
                        <label htmlFor="prompt" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Task Prompt</label>
                        <textarea
                            id="prompt"
                            rows="4"
                            className="w-full p-3 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg text-white"
                            value={prompt}
                            onChange={(e) => setPrompt(e.target.value)}
                            required
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Schedule</label>
                        <div className="flex items-center gap-2">
                            <span className="text-gray-700 dark:text-gray-300">Run every</span>
                            <input
                                type="number"
                                min="1"
                                className="w-24 p-2 text-center bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg text-white"
                                value={interval}
                                onChange={(e) => setInterval(e.target.value)}
                                required
                            />
                            <select
                                className="p-2 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg text-white"
                                value={unit}
                                onChange={(e) => setUnit(e.target.value)}
                            >
                                <option value="minutes">Minutes</option>
                                <option value="hours">Hours</option>
                                <option value="days">Days</option>
                            </select>
                        </div>
                    </div>

                    <div className="flex justify-between items-center pt-4"> {/* Changed to justify-between */}
                        <button
                            type="button"
                            onClick={handleDelete}
                            disabled={isDeleting || isUpdating} // Disable delete while updating or deleting
                            className="px-6 py-2 text-sm font-semibold text-white-700 bg-red-100 dark:bg-red-600 rounded-lg hover:bg-red-200 disabled:opacity-50 flex items-center gap-2"
                        >
                            <Trash2 className="w-4 h-4" />
                            {isDeleting ? 'Deleting...' : 'Delete Task'}
                        </button>

                        <div className="flex gap-4">
                            <button
                                type="button"
                                onClick={onClose}
                                className="px-6 py-2 text-sm font-semibold text-white-700 bg-gray-100 dark:bg-gray-600 rounded-lg hover:bg-gray-200"
                            >
                                Cancel
                            </button>
                            <button
                                type="submit"
                                disabled={isUpdating || isDeleting} // Disable save while deleting or updating
                                className="px-6 py-2 text-sm font-semibold text-white bg-blue-600 rounded-lg hover:bg-blue-700 disabled:bg-blue-400"
                            >
                                {isUpdating ? 'Saving...' : 'Save Changes'}
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default EditTaskModal;
