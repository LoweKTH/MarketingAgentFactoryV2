// src/pages/ScheduledTasksPage/components/ScheduledTaskRow.jsx

import React from 'react';
import { Calendar, Clock, Play, Pause, RotateCw, Edit, Trash2 } from 'lucide-react'; // Import Trash2

// --- Helper Function for Date Formatting (oförändrad) ---
const formatDateTime = (isoString) => {
    if (!isoString) return "N/A";
    try {
        return new Date(isoString).toLocaleString('sv-SE', {
            year: 'numeric', month: 'short', day: 'numeric',
            hour: '2-digit', minute: '2-digit',
        });
    } catch (error) {
        return "Invalid Date";
    }
};

const ScheduledTaskRow = ({ task, onToggleActive, isToggling, onEdit, onDelete }) => { // Add onDelete prop
    const { id, prompt, humanReadableCronExpression, nextRunTime, lastRunTime, active } = task;

    // Styling för status-indikatorn
    const statusTextColor = active ? 'text-green-800 dark:text-green-300' : 'text-gray-600 dark:text-gray-400';
    const statusDotColor = active ? 'bg-green-500' : 'bg-gray-400';

    return (
        <tr className={`border-b dark:border-gray-700 ${isToggling ? 'opacity-50' : ''} hover:bg-gray-50 dark:hover:bg-gray-800`}>
            {/* Status Cell */}
            <td className="px-6 py-4">
                <div className={`inline-flex items-center text-sm font-medium ${statusTextColor}`}>
                    <span className={`w-2 h-2 mr-2 rounded-full ${statusDotColor}`}></span>
                    {active ? 'Active' : 'Inactive'}
                </div>
            </td>

            {/* Prompt Cell */}
            <td className="px-6 py-4 font-medium text-gray-900 dark:text-white max-w-md truncate" title={prompt}>
                {prompt}
            </td>

            {/* Schedule Cell */}
            <td className="px-6 py-4 text-gray-600 dark:text-gray-300 whitespace-nowrap">
                {humanReadableCronExpression}
            </td>

            {/* Next Run Cell */}
            <td className="px-6 py-4 text-gray-600 dark:text-gray-300 whitespace-nowrap">
                {formatDateTime(nextRunTime)}
            </td>

            {/* Last Run Cell */}
            <td className="px-6 py-4 text-gray-600 dark:text-gray-300 whitespace-nowrap">
                {formatDateTime(lastRunTime)}
            </td>

            {/* Actions Cell */}
            <td className="px-6 py-4 text-right">
                <div className="flex items-center justify-end gap-2">
                    {/* Delete Button */}
                    <button
                        onClick={() => onDelete(id)} // Call onDelete with task ID
                        className="p-2 rounded-lg text-sm font-semibold text-red-600 dark:text-red-300 hover:bg-red-200 dark:hover:bg-red-700"
                        aria-label="Delete Task"
                    >
                        <Trash2 className="w-4 h-4" />
                    </button>

                    {/* Edit Button */}
                    <button
                        onClick={() => onEdit(task)} // Pass the whole task object
                        className="p-2 rounded-lg text-sm font-semibold text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-700"
                        aria-label="Edit Task"
                    >
                        <Edit className="w-4 h-4" />
                    </button>

                    {/* Toggle Active/Pause Button */}
                    <button
                        onClick={() => onToggleActive(id)}
                        className={`p-2 rounded-lg text-sm font-semibold transition-colors duration-200 flex items-center gap-1
                            ${active
                            ? 'bg-yellow-100 text-yellow-800 hover:bg-yellow-200 dark:bg-yellow-700 dark:text-yellow-100 dark:hover:bg-yellow-600'
                            : 'bg-green-100 text-green-800 hover:bg-green-200 dark:bg-green-700 dark:text-green-100 dark:hover:bg-green-600'
                        }
                            ${isToggling ? 'opacity-50 cursor-not-allowed' : ''}
                        `}
                        disabled={isToggling}
                        aria-label={active ? 'Pause Task' : 'Resume Task'}
                    >
                        {active ? <Pause className="w-4 h-4" /> : <Play className="w-4 h-4" />}
                        <span>{active ? 'Pause' : 'Resume'}</span>
                    </button>
                </div>
            </td>
        </tr>
    );
};

export default ScheduledTaskRow;
