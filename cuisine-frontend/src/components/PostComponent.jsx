import React, { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { createPost, getPost, updatePost } from '../services/PostService'
import AuthService from '../services/AuthService'
import ImageUpload from './ImageUpload';

const PostComponent = () => {

    const [title, setTitle] = useState('')
    const [body, setBody] = useState('')
    const [imageUrl, setImageUrl] = useState('')
    const {postId} = useParams();
    const currentUser = AuthService.getCurrentUser();

    const [isDataLoaded, setIsDataLoaded] = useState(false);

    const [errors, setErrors] = useState({
        title: '',
        body: '',
        imageUrl: ''
    })

    const navigator = useNavigate();

    //check login status
    useEffect(() => {
        if(!currentUser) {
            alert('Please login to create or edit posts');
            navigator('/login');
            return;
        }
    }, [currentUser, navigator]);

    useEffect(() => {
        if(postId) {
            getPost(postId).then((response) => {

                if(currentUser && response.data.userId !== currentUser.id) {
                    alert('You can only edit your own posts');
                    navigator('/');
                    return;
                }

                setTitle(response.data.title);
                setBody(response.data.body);
                setImageUrl(response.data.imageUrl);
                setIsDataLoaded(true);

            }).catch(error => {
                console.error(error);
                alert('Failed to load post');
                navigator('/')
            })
        }
    }, [postId, isDataLoaded])

    const handleTitle = (e) => {
        setTitle(e.target.value);
    }

    const handleBody = (e) => {
        setBody(e.target.value);
    }

    const handleImageUrl = (e) => {
        setImageUrl(e.target.value);
    }

    function saveOrUpdatePost(e) {
        e.preventDefault()

        if(!currentUser) {
            alert('Please login first');
            navigator('/login');
            return;
        }

        console.log('Current User:', currentUser);
        console.log('Auth Header:', AuthService.getAuthHeader());
        console.log('Token:', AuthService.getToken());

        if(checkValid()) {
            const post = {title, body, imageUrl}
            console.log(post)

            if(postId) {
                updatePost(postId, post).then((response) => {
                    console.log(response.data)
                    navigator('/')
                }).catch(error => {

                    console.error('Full error:', error);
                    console.error('Error response:', error.response);
                    console.error('Error status:', error.response?.status);
                    console.error('Error data:', error.response?.data);

                    console.error(error);
                    if (error.response?.status === 403) {
                        alert('You can only edit your own posts');
                    } else {
                        alert('Failed to update post. Please try again')
                    }
                })
            } else {
                createPost(post).then((response) => {
                    console.log(response.data)
                    navigator('/')
                }).catch(error => {
                    console.error(error);
                    alert('Failed to create post. Please make sure you are logged in.');
                })
            }
        }
    }

    function checkValid() {
        let valid = true;

        const errorsCopy = {... errors}

        if(title.trim()) {
            errorsCopy.title = '';
        } else {
            errorsCopy.title = 'Cannot be empty';
            valid = false;
        }

        if(body.trim()) {
            errorsCopy.body = '';
        } else {
            errorsCopy.body = 'Cannot be empty';
            valid = false;
        }

        setErrors(errorsCopy);

        return valid;
    }

    function headline() {
        if(postId) {
            return <h2 className='text-center fw-bold' style={{color: '#66eacbff'}}>✏️ Edit your blog</h2>
        } else {
            return <h2 className='text-center fw-bold' style={{color: '#66eacbff'}}>✨ Compose a new blog</h2>
        }
    }

    function handleCancel() {
        navigator('/');
    }

    //if not log in, will not display the form
    if (!currentUser) {
        return null;
    }

    return (
        <div className='container mt-5' style={{ maxWidth: '700px' }}>
            <br/> <br/>
            {headline()}
            <br/>
            <div className='form-card'>
                <form>
                    <div className='mb-4'>
                        <label className='form-label'>📝 title</label>
                        <input
                            type='text'
                            placeholder='Write down your title here...'
                            name='title'
                            value={title}
                            className={`form-control ${ errors.title ? 'is-invalid' : ''}`}
                            onChange={handleTitle}
                        />
                        { errors.title && <div className='invalid-feedback'>{errors.title}</div>}
                    </div>

                    <div className='mb-4'>
                        <label className='form-label'>📄 Body</label>
                        <textarea
                            placeholder='Share your idea with us...'
                            name='body'
                            value={body}
                            className={`form-control ${ errors.body ? 'is-invalid' : ''}`}
                            onChange={handleBody}
                            rows='8'
                            style={{resize: 'vertical'}}
                        />
                        { errors.body && <div className='invalid-feedback'>{errors.body}</div>}
                    </div>

                    <ImageUpload 
                        onUploadSuccess={(url) => setImageUrl(url)}
                        currentImageUrl={imageUrl}
                    />

                    <div className='mb-4'>
                        <label className='form-label'>🔗 Or paste image URL</label>
                        <input
                            type='text'
                            placeholder='https://example.com/image.jpg'
                            name='imageUrl'
                            value={imageUrl}
                            className='form-control'
                            onChange={handleImageUrl}
                        />
                    </div>

                    {imageUrl && (
                        <div className='mb-4'>
                            <label className='form-label'>Preview</label>
                            <img 
                                src={imageUrl} 
                                alt="preview" 
                                className='img-fluid rounded'
                                style={{maxHeight: '300px', objectFit: 'cover', width: '100%'}}
                                onError={(e) => {
                                    e.target.style.display = 'none';
                                }}
                            />
                        </div>
                    )}

                    <div className='d-flex gap-2'>
                        <button 
                            type='button' 
                            className='btn btn-secondary flex-grow-1' 
                            onClick={handleCancel}
                            style={{
                                borderRadius: '25px',
                                padding: '12px 30px',
                                fontWeight: '600'
                            }}
                        >
                            Cancel
                        </button>
                        <button 
                            type='submit' 
                            className='btn btn-success flex-grow-1' 
                            onClick={saveOrUpdatePost}
                        >
                            {postId ? '💾 Save' : '📤 Upload'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    )
}

export default PostComponent

