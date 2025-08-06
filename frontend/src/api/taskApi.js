// src/api/taskApi.js
import axiosInstance from './axiosInstance';

/**
 * Fetches all scheduled tasks for the authenticated user.
 * @returns {Promise<Array<object>>} A promise that resolves to an array of task objects.
 * @throws {Error} If the API call fails.
 */
export const fetchTasks = async () => {
    try {
        const response = await axiosInstance.get('/tasks');
        return response.data;
    } catch (error) {
        console.error("Error in fetchTasks:", error);
        throw error.response?.data?.message || error.message || "Failed to fetch tasks";
    }
};

/**
 * Toggles the active state of a scheduled task.
 * @param {number} taskId The ID of the task to toggle.
 * @returns {Promise<object>} A promise that resolves to the updated task object.
 * @throws {Error} If the API call fails.
 */
export const toggleTaskActiveState = async (taskId) => {
    try {
        const response = await axiosInstance.put(`/tasks/${taskId}/toggle`);
        return response.data;
    } catch (error) {
        console.error("Error in toggleTaskActiveState:", error);
        throw error.response?.data?.message || error.message || "Failed to toggle task active state";
    }
};

/**
 * Updates a task's prompt and cron expression.
 * @param {number} taskId The ID of the task to update.
 * @param {{ prompt: string, cronExpression: string }} taskData The data to update.
 * @returns {Promise<object>} A promise that resolves to the updated task object.
 */
export const updateTask = async (taskId, taskData) => {
    try {
        const response = await axiosInstance.put(`/tasks/${taskId}`, taskData);
        return response.data;
    } catch (error) {
        console.error("Error in updateTask:", error);
        throw error.response?.data?.message || error.message || "Failed to update task";
    }
};


/**
 * Deletes a scheduled task.
 * @param {number} taskId The ID of the task to delete.
 * @returns {Promise<void>} A promise that resolves when the task is successfully deleted.
 * @throws {Error} If the API call fails.
 */
export const deleteTask = async (taskId) => {
    try {
        await axiosInstance.delete(`/tasks/${taskId}`);
    } catch (error) {
        console.error("Error in deleteTask:", error);
        throw error.response?.data?.message || error.message || "Failed to delete task";
    }
};
