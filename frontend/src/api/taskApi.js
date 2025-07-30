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


export const deleteTask = async (taskId) => {

}