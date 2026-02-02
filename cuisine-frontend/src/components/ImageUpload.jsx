import React, { useState } from 'react';

const ImageUpload = ({ onUploadSuccess, currentImageUrl }) => {
    const [uploading, setUploading] = useState(false);
    const [preview, setPreview] = useState(currentImageUrl || '');
    const [error, setError] = useState('');

    const handleFileChange = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        if (!file.type.startsWith('image/')) {
            setError('Please select an image file');
            return;
        }

        if (file.size > 5 * 1024 * 1024) {
            setError('File size must be less than 5MB');
            return;
        }

        setError('');
        setUploading(true);

        const reader = new FileReader();
        reader.onloadend = () => {
            setPreview(reader.result);
        };
        reader.readAsDataURL(file);

        const formData = new FormData();
        formData.append('file', file);

        try {
            const response = await fetch('http://localhost:8080/api/upload/image', {
                method: 'POST',
                body: formData
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to upload image');
            }

            const data = await response.json();
            const imageUrl = data.imageUrl;
            onUploadSuccess(imageUrl);
            setError('');
        } catch (err) {
            console.error('Upload error:', err);
            setError(err.message || 'Failed to upload image');
            setPreview('');
        } finally {
            setUploading(false);
        }
    };

    const handleRemove = () => {
        setPreview('');
        onUploadSuccess('');
        setError('');
    };

    return (
        <div className="image-upload-container mb-4">
            <label className="form-label">🖼️ Upload Image</label>
            
            <div className="upload-area">
                {!preview ? (
                    <label className="upload-label">
                        <input
                            type="file"
                            accept="image/*"
                            onChange={handleFileChange}
                            disabled={uploading}
                            style={{ display: 'none' }}
                        />
                        <div className="upload-placeholder">
                            {uploading ? (
                                <div>
                                    <div className="spinner-border text-primary" role="status">
                                        <span className="visually-hidden">Uploading...</span>
                                    </div>
                                    <p className="mt-2">Uploading...</p>
                                </div>
                            ) : (
                                <div>
                                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{margin: '0 auto', display: 'block', color: '#667eea'}}>
                                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                                        <polyline points="17 8 12 3 7 8" />
                                        <line x1="12" y1="3" x2="12" y2="15" />
                                    </svg>
                                    <p className="mt-2" style={{margin: '0.5rem 0 0.25rem', fontWeight: 500, color: '#2d3748'}}>Click to upload image</p>
                                    <small className="text-muted">PNG, JPG, GIF up to 5MB</small>
                                </div>
                            )}
                        </div>
                    </label>
                ) : (
                    <div className="image-preview" style={{position: 'relative', padding: '1rem'}}>
                        <img 
                            src={preview} 
                            alt="Preview" 
                            style={{width: '100%', maxHeight: '400px', objectFit: 'contain', borderRadius: '8px'}}
                        />
                        <button
                            type="button"
                            className="btn btn-danger btn-sm"
                            onClick={handleRemove}
                            style={{position: 'absolute', top: '1.5rem', right: '1.5rem', borderRadius: '20px', padding: '6px 16px', fontSize: '0.875rem'}}
                        >
                            ✕ Remove
                        </button>
                    </div>
                )}
            </div>

            {error && (
                <div className="alert alert-danger mt-2 mb-0">
                    {error}
                </div>
            )}

            <small className="text-secondary d-block mt-2">
                Or paste an image URL in the field below
            </small>
        </div>
    );
};

export default ImageUpload;