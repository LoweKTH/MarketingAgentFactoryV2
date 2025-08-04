// src/pages/ScheduledTasksPage/components/ScheduledTasksTable.jsx

import React from 'react';
import ScheduledTaskRow from './ScheduledTaskRow';

const ScheduledTasksTable = ({ tasks, onToggleActive, togglingTaskId, onEdit  }) => {
    return (
        <div className="relative overflow-x-auto shadow-md rounded-lg">
            <table className="w-full text-sm text-left text-gray-500 dark:text-gray-400">
                <thead className="text-xs text-gray-700 uppercase bg-gray-100 dark:bg-gray-700 dark:text-gray-400">
                <tr>
                    <th scope="col" className="px-6 py-3">Status</th>
                    <th scope="col" className="px-6 py-3">Task Prompt</th>
                    <th scope="col" className="px-6 py-3">Schedule</th>
                    <th scope="col" className="px-6 py-3">Next Run</th>
                    <th scope="col" className="px-6 py-3">Last Run</th>
                    <th scope="col" className="px-6 py-3">Actions</th>
                </tr>
                </thead>
                <tbody>
                {tasks.map(task => (
                    <ScheduledTaskRow
                        key={task.id}
                        task={task}
                        onToggleActive={onToggleActive}
                        isToggling={togglingTaskId === task.id}
                        onEdit={onEdit}
                    />
                ))}
                </tbody>
            </table>
            {tasks.length === 0 && (
                <div className="text-center py-8 text-gray-500 dark:text-gray-400">
                    No scheduled tasks found.
                </div>
            )}
        </div>
    );
};

export default ScheduledTasksTable;