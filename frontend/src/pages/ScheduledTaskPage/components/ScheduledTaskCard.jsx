// src/pages/ScheduledTasksPage/components/ScheduledTaskCard.jsx

import React from 'react';
import { Calendar, Clock, Play, Pause, RotateCw } from 'lucide-react';

// --- Helper Function for Date Formatting ---
const formatDateTime = (isoString) => {
    if (!isoString) return "N/A";
    try {
        return new Date(isoString).toLocaleString('en-US', {
            year: 'numeric', month: 'long', day: 'numeric',
            hour: '2-digit', minute: '2-digit', hour12: true,
        });
    } catch (error) {
        return "Invalid Date";
    }
};

const ScheduledTaskCard = ({ task, onToggleActive, isToggling }) => {
    // Destructure the new humanReadableCronExpression from the task object
    const { id, prompt, cronExpression, humanReadableCronExpression, nextRunTime, lastRunTime, active } = task;

    const statusBgColor = active ? 'bg-green-100 dark:bg-green-900/50' : 'bg-gray-100 dark:bg-gray-800';
    const statusTextColor = active ? 'text-green-800 dark:text-green-300' : 'text-gray-600 dark:text-gray-400';
    const statusDotColor = active ? 'bg-green-500' : 'bg-gray-400';

    return (
        <div className={`
            ${statusBgColor} border border-gray-200 dark:border-gray-700/80
            rounded-2xl shadow-lg hover:shadow-xl transition-all duration-300
            flex flex-col p-6 space-y-4 ${isToggling ? 'opacity-50 cursor-not-allowed' : ''}`
        }>
            <div className="flex-grow">
                <p className="text-lg font-semibold text-gray-800 dark:text-gray-100">{prompt}</p>
            </div>
            <div className="space-y-3 pt-4 border-t border-gray-300 dark:border-gray-600">
                <div className="flex items-center text-gray-600 dark:text-gray-300">
                    <Calendar className="w-5 h-5 mr-3 text-indigo-500 dark:text-indigo-400" />
                    <span className="font-medium w-24">Next Run:</span>
                    <span className="text-gray-800 dark:text-gray-200">{formatDateTime(nextRunTime)}</span>
                </div>
                <div className="flex items-center text-gray-600 dark:text-gray-300">
                    <Clock className="w-5 h-5 mr-3 text-indigo-500 dark:text-indigo-400" />
                    <span className="font-medium w-24">Last Run:</span>
                    <span className="text-gray-800 dark:text-gray-200">{formatDateTime(lastRunTime)}</span>
                </div>
                <div className="flex items-center text-gray-600 dark:text-gray-300">
                    <RotateCw className="w-5 h-5 mr-3 text-indigo-500 dark:text-indigo-400" />
                    <span className="font-medium w-24">Schedule:</span>
                    {/* Display the human-readable cron expression */}
                    <span className="text-gray-800 dark:text-gray-200">{humanReadableCronExpression}</span>
                    {/* Optional: If you want to show the raw cron expression on hover or in a smaller text */}
                    {/* <span className="text-gray-500 dark:text-gray-500 text-xs ml-2" title={cronExpression}>
                        ({cronExpression})
                    </span> */}
                </div>
            </div>
            <div className="flex items-center justify-between pt-4 border-t border-gray-300 dark:border-gray-600">
                <div className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${statusBgColor} ${statusTextColor}`}>
                    <span className={`w-2 h-2 mr-2 rounded-full ${statusDotColor}`}></span>
                    {active ? 'Active' : 'Inactive'}
                </div>
                <button
                    onClick={() => onToggleActive(id)}
                    disabled={isToggling}
                    className={`flex items-center justify-center space-x-2 px-4 py-2 rounded-lg text-sm font-semibold transition-colors duration-200 disabled:opacity-70 disabled:cursor-wait ${active ? 'bg-yellow-500 hover:bg-yellow-600 text-white' : 'bg-blue-500 hover:bg-blue-600 text-white'} focus:outline-none focus:ring-2 focus:ring-offset-2 dark:focus:ring-offset-gray-900 ${active ? 'focus:ring-yellow-500' : 'focus:ring-blue-500'}`}
                >
                    {active ? <Pause className="w-4 h-4" /> : <Play className="w-4 h-4" />}
                    <span>{active ? 'Pause' : 'Resume'}</span>
                </button>
            </div>
        </div>
    );
};

export default ScheduledTaskCard;