import React, { useState } from "react";

const TestFacebookPostPage = () => {
    const [message, setMessage] = useState("");
    const [result, setResult] = useState(null);
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setResult(null);
        try {
            const params = new URLSearchParams({ message });
            const response = await fetch(
                `http://localhost:8080/api/auth/facebook/post-to-page?${params.toString()}`,
                {
                    method: "GET",
                    credentials: "include",
                }
            );
            const text = await response.text();
            setResult(text);
        } catch (err) {
            setResult("Error: " + err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="max-w-xl mx-auto p-6 bg-white rounded-xl shadow-md mt-10">
            <h2 className="text-2xl font-bold mb-4 text-center">Test Facebook Page Post</h2>
            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <label className="block font-semibold mb-1">Message</label>
                    <textarea
                        className="w-full border rounded px-3 py-2"
                        value={message}
                        onChange={(e) => setMessage(e.target.value)}
                        required
                    />
                </div>
                <button
                    type="submit"
                    className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700 transition-colors"
                    disabled={loading}
                >
                    {loading ? "Posting..." : "Post to Facebook Page"}
                </button>
            </form>
            {result && (
                <div className="mt-6 p-4 bg-gray-100 rounded text-sm break-all">
                    <strong>Result:</strong>
                    <pre>{result}</pre>
                </div>
            )}
        </div>
    );
};

export default TestFacebookPostPage;
