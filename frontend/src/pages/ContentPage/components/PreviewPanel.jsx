// src/pages/ContentPage/components/PreviewPanel.jsx

import React from 'react';
import PostButton from './PostButton.jsx';
import { Image as ImageIcon } from 'lucide-react'; // Importera en ikon för platshållaren

function PreviewPanel({ content, imageUrl, onPostClick, isPosting }) {
    return (
        <div className="flex flex-col h-full p-4">
            <h2 className="text-2xl font-bold mb-6 text-white text-center">Generated Content Preview</h2>
            <div className="bg-gray-700 rounded-lg shadow-lg p-6 w-full max-w-md mx-auto flex-grow overflow-hidden flex flex-col">

                {/* ---- NY KONDITIONELL LOGIK ---- */}
                {imageUrl ? (
                    // 1. Om en bild-URL finns, visa bilden.
                    <div className="flex-grow flex items-center justify-center overflow-hidden">
                        <img
                            src={imageUrl}
                            alt="AI Generated Content"
                            className="max-w-full max-h-full object-contain rounded-md"
                        />
                    </div>
                ) : content ? (
                    // 2. Annars, om text-innehåll finns, visa det.
                    <>
                        <div className="flex items-center mb-4">
                            <div className="w-10 h-10 bg-indigo-500 rounded-full flex items-center justify-center text-white font-bold text-lg">AI</div>
                            <div className="ml-3">
                                <p className="font-semibold text-white">AI Agent</p>
                                <p className="text-sm text-gray-300">Just now</p>
                            </div>
                        </div>
                        <div className="flex-grow overflow-y-auto pr-2">
                            <p className="text-gray-200 whitespace-pre-wrap">{content}</p>
                        </div>
                    </>
                ) : (
                    // 3. Annars, visa platshållaren.
                    <div className="text-center text-gray-400 py-10 flex-grow flex items-center justify-center">
                        <div className="text-center text-gray-400 dark:text-gray-500">
                            <ImageIcon className="w-16 h-16 mx-auto mb-4" />
                            <p className="font-medium">Content will appear here.</p>
                            <p className="text-sm">Ask the agent to generate a post or an image.</p>
                        </div>
                    </div>
                )}
                {/* ---------------------------------- */}

                {/* Post-knappen visas bara om det finns text-innehåll att posta */}
                {content && !imageUrl && (
                    <div className="mt-6">
                        <PostButton
                            onClick={onPostClick}
                            isLoading={isPosting}
                        />
                    </div>
                )}

            </div>
        </div>
    );
}

export default PreviewPanel;