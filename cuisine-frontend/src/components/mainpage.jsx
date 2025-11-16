import React, {useState, useEffect} from "react"
import { allPost, deletePost } from "../services/PostService";
import { useNavigate } from "react-router-dom";
import AuthService from "../services/AuthService";

const mainpage = () => {

    const [posts, setPost] = useState([]);
    const currentUser = AuthService.getCurrentUser();

    const navigator = useNavigate();

    useEffect(() => {
        getAllPost();
    }, [])

    function getAllPost() {
        allPost().then((response) => {
            setPost(response.data);
        }).catch(error => {
            console.error(error);
        })
    }

    function addPost() { 
        if (!currentUser) {
            alert('Please login to create a post');
            navigator('/login');
            return;
        }

        navigator('/add-post')
    }

    function updatePost(postId, postUserId) {
        if (!currentUser) {
            alert('Please login to edit')
            navigator('/login')
            return;
        }

        if (currentUser.id !== postUserId) {
            alert('You can only edit your own posts');
            return;
        }

        navigator(`/modify-post/${postId}`)
    }

    function removePost(id, postUserId) {
        if(!currentUser) {
            alert('Please login to delete');
            navigator('/login');
            return;
        }

        if (currentUser.id !== postUserId) {
            alert('You can only delete your own posts');
            return;
        }

        if (window.confirm('Are you sure you want to delete this post')) {
            deletePost(id).then((response) => {
                getAllPost();
            }).catch(error => {
                console.error('Failed to delete post');
            }) 
        }

    }

    function formatDate(dateString) {
        if (!dateString) return '';

        const date = new Date(dateString);;
        const now = new Date();
        const diffInSeconds = Math.floor((now - date) / 1000);

        if (diffInSeconds < 60) {
            return 'Just now';
        }

        const diffInMinutes = Math.floor(diffInSeconds / 60);
        if(diffInMinutes < 60) {
            return `${diffInMinutes} minute${diffInMinutes > 1 ? 's' : ''} ago`;
        }

        const diffInHours= Math.floor(diffInMinutes / 60);
        if (diffInHours < 24) {
            return `${diffInHours} hour${diffInHours > 1 ? 's' : ''} ago`;
        }

        const diffInDays = Math.floor(diffInHours / 24);
        if (diffInDays < 7) {
            return `${diffInDays} day${diffInDays > 1 ? 's' : ''} ago`;
        }

        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    return (
        <div className='container my-5' style={{ maxWidth: '900px'}}>
            <br/> <br/>

            <div className="page-header">
                <div className="d-flex justify-content-between align-items-center">
                    <h2 className="fw-bold">✨ Cuisine Blog</h2>
                    <button className="btn btn-primary" onClick={addPost}>
                        ✏️ Compose
                    </button>
                </div>
            </div>

            {posts.length === 0 ? (
                <div className='empty-state'>
                    <h3>📝 No blogs yet</h3>
                    <p className='text-secondary'>Get started your first blog!</p>
                </div>
            ): (
                posts.map(post => (
                    <div key={post.postId} className="card mb-4 shadow-sm">

                        {post.imageUrl && (
                            <img src={post.imageUrl} alt={post.title} className="img-fluid" />
                        )}

                        <h3 className="card-title">{post.title}</h3>

                        <div className="post-meta">
                            <span className="author-info">
                                👤 {post.authorName || 'Anonymous'}
                            </span>
                            <span className="post-time">
                                🕒 {formatDate(post.createdAt)}
                            </span>
                        </div>

                        <p className="card-text">{post.body}</p>
                        {/* {post.imageUrl && (
                            <img src={post.imageUrl} alt={post.title} className="img-fluid" />
                        )} */}

                        <div className="card-buttons">
                            <div className="d-flex justify-content-end gap-2">
                                {/* Edit and Delete only available for the authors*/}
                                {currentUser && currentUser.id === post.userId && (
                                    <>
                                        <button
                                            className="btn btn-info btn-sm"
                                            onClick={() => updatePost(post.postId, post.userId)}
                                        >
                                            ✏️ Edit
                                        </button>

                                        <button
                                            className="btn btn-danger btn-sm"
                                            onClick={() => removePost(post.postId, post.userId)}
                                        >
                                            🗑️ Delete
                                        </button>
                                    </>
                                )}
                            </div>
                        </div>

                    </div>
                ))
            )};
        </div>
    )

};

export default mainpage