import React, { useState } from 'react';
import { postTweet } from '../../api/authApi'; // Adjust the import path as necessary

const TweetComposer = ({ onTweetPosted }) => {
    const [tweetContent, setTweetContent] = useState('');
    const [isPosting, setIsPosting] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    const maxLength = 280;
    const remainingChars = maxLength - tweetContent.length;

    const handleSubmit = async () => {

        if (!tweetContent.trim()) {
            setError('Tweet content cannot be empty');
            return;
        }

        if (tweetContent.length > maxLength) {
            setError('Tweet exceeds character limit');
            return;
        }

        setIsPosting(true);
        setError('');
        setSuccess('');

        try {
            // You'll need to import this API function
            const response = await postTweet(tweetContent.trim());

            setSuccess(`Tweet posted successfully! Tweet ID: ${response.tweetId}`);
            setTweetContent('');

            // Callback to parent component if provided
            if (onTweetPosted) {
                onTweetPosted(response);
            }
        } catch (err) {
            setError(err.message || 'Failed to post tweet');
        } finally {
            setIsPosting(false);
        }
    };

    const handleInputChange = (e) => {
        setTweetContent(e.target.value);
        setError(''); // Clear error when user types
        setSuccess(''); // Clear success when user types
    };

    return (
        <div className="bg-white p-6 rounded-xl shadow-lg hover:shadow-xl transition-shadow duration-300">
            <h2 className="text-2xl font-bold text-gray-800 mb-4 flex items-center">
                <svg className="w-6 h-6 mr-2 text-blue-400" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M22.46 6c-.8.36-1.64.6-2.5.7.9-.54 1.5-1.4 1.8-2.4-.8.5-1.7.9-2.7 1.1-.8-.8-1.9-1.3-3.2-1.3-2.4 0-4.3 1.9-4.3 4.3 0 .34.04.67.1.98-3.6-.18-6.8-1.9-9-4.5-.4.6-.6 1.4-.6 2.2 0 1.5.8 2.8 2 3.6-.7 0-1.4-.2-2-.5v.05c0 2.1 1.5 3.8 3.5 4.2-.3.08-.7.13-1 .13-.24 0-.47-.02-.7-.07.5 1.7 2.1 2.9 4 2.9-1.5 1.2-3.4 1.9-5.5 1.9-.36 0-.7-.02-1.04-.06C2.9 20.3 5.3 21 7.9 21c9.4 0 14.5-7.8 14.5-14.5 0-.22-.01-.44-.02-.66z" />
                </svg>
                Post Tweet
            </h2>

            <div className="space-y-4">
                <div>
          <textarea
              value={tweetContent}
              onChange={handleInputChange}
              placeholder="What's happening?"
              className="w-full p-3 border border-gray-300 rounded-lg resize-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              rows="4"
              disabled={isPosting}
          />
                    <div className="flex justify-between items-center mt-2">
            <span className={`text-sm ${remainingChars < 0 ? 'text-red-500' : remainingChars < 20 ? 'text-yellow-500' : 'text-gray-500'}`}>
              {remainingChars} characters remaining
            </span>
                        {remainingChars < 0 && (
                            <span className="text-red-500 text-sm font-medium">
                {Math.abs(remainingChars)} over limit
              </span>
                        )}
                    </div>
                </div>

                {error && (
                    <div className="p-3 bg-red-100 border border-red-400 text-red-700 rounded-lg">
                        <div className="flex items-center">
                            <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                            </svg>
                            {error}
                        </div>
                    </div>
                )}

                {success && (
                    <div className="p-3 bg-green-100 border border-green-400 text-green-700 rounded-lg">
                        <div className="flex items-center">
                            <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                            </svg>
                            {success}
                        </div>
                    </div>
                )}

                <button
                    type="button"
                    onClick={handleSubmit}
                    disabled={isPosting || !tweetContent.trim() || remainingChars < 0}
                    className={`w-full py-3 px-4 rounded-lg font-medium transition-colors duration-200 flex items-center justify-center ${
                        isPosting || !tweetContent.trim() || remainingChars < 0
                            ? 'bg-gray-400 text-white cursor-not-allowed'
                            : 'bg-blue-500 text-white hover:bg-blue-600'
                    }`}
                >
                    {isPosting ? (
                        <>
                            <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                            </svg>
                            Posting...
                        </>
                    ) : (
                        <>
                            <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
                            </svg>
                            Post Tweet
                        </>
                    )}
                </button>
            </div>
        </div>
    );
};

export default TweetComposer;