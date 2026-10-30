import React, { useState, useEffect, useCallback } from "react"
import { useNavigate } from "react-router-dom";
import { allPost, deletePost } from "../services/PostService";
import AuthService from "../services/AuthService";

const PAGE_SIZE = 5;

const MainPage = () => { 

    const [posts, setPosts] = useState([]);
    const [pageNo, setPageNo] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [isLast, setIsLast] = useState(true);
    const [searchInput, setSearchInput] = useState('');  // what the user is typing
    const [activeQuery, setActiveQuery] = useState('');  // the query actually sent to the backend
    const currentUser = AuthService.getCurrentUser();
    const navigator = useNavigate();

    const getAllPost = useCallback(() => {
        allPost(pageNo, PAGE_SIZE, activeQuery).then((response) => {
            const data = response.data;

            // the current page became empty (e.g. its last post was deleted): go back one page
            if (data.content.length === 0 && pageNo > 0) {
                setPageNo(pageNo - 1);
                return;
            }

            setPosts(data.content);
            setTotalPages(data.totalPages);
            setIsLast(data.last);
        }).catch(error => {
            console.error('Failed to load posts', error);
        })
    }, [pageNo, activeQuery]);

    // reload whenever the page or the search query changes
    useEffect(() => {
        getAllPost();
    }, [getAllPost])

    // handle search function
    function handleSearch(e) {
        e.preventDefault();
        setPageNo(0); // reset to the first page when searching
        setActiveQuery(searchInput.trim());
    }

    // handle page change
    function handlePageChange(newPage) {
        if (newPage >= 0 && newPage < totalPages) {
            setPageNo(newPage);
            window.scrollTo(0, 0); // scroll to the top when page changes
        }
    }

    function addPost() {
        // only logged in users can create posts
        if (!currentUser) {
            alert('Please login to create a post');
            navigator('/login');
            return;
        }
        navigator('/add-post')
    }

    function updatePost(postId, postUserId) {
        // only logged in users can edit posts
        if (!currentUser) {
            alert('Please login to edit')
            navigator('/login')
            return;
        }

        // only the author can edit their post
        if (currentUser.id !== postUserId) {
            alert('You can only edit your own posts');
            return;
        }

        navigator(`/modify-post/${postId}`)
    }

    function removePost(id, postUserId) {
        if (!currentUser) {
            alert('Please login to delete');
            navigator('/login');
            return;
        }

        if (currentUser.id !== postUserId) {
            alert('You can only delete your own posts');
            return;
        }

        if (window.confirm('Are you sure you want to delete this post')) {
            deletePost(id).then(() => {
                // refresh after delete
                getAllPost();
            }).catch(error => {
                console.error('Failed to delete post', error);
                if (error.response?.status === 401) {
                    AuthService.handleSessionExpired();
                } else {
                    alert(error.response?.data?.message || 'Failed to delete post');
                }
            })
        }
    }

    function formatDate(dateString) {
        if (!dateString) return '';

        const date = new Date(dateString);
        const now = new Date();
        const diffInSeconds = Math.floor((now - date) / 1000);

        if (diffInSeconds < 60) return 'Just now';

        const diffInMinutes = Math.floor(diffInSeconds / 60);
        if (diffInMinutes < 60) return `${diffInMinutes} minute${diffInMinutes > 1 ? 's' : ''} ago`;

        const diffInHours = Math.floor(diffInMinutes / 60);
        if (diffInHours < 24) return `${diffInHours} hour${diffInHours > 1 ? 's' : ''} ago`;

        const diffInDays = Math.floor(diffInHours / 24);
        if (diffInDays < 7) return `${diffInDays} day${diffInDays > 1 ? 's' : ''} ago`;

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

            <div className="page-header mb-4">
                <div className="d-flex justify-content-between align-items-center mb-3">
                    <h2 className="fw-bold">✨ Cuisine Blog</h2>
                    <button className="btn btn-primary" onClick={addPost}>
                        ✏️ Compose
                    </button>
                </div>

                {/* search bar */}
                <form onSubmit={handleSearch}>
                    <div className="input-group">
                        <input
                            type="text"
                            className="form-control"
                            placeholder="Search posts by title..."
                            value={searchInput}
                            onChange={(e) => setSearchInput(e.target.value)}
                        />
                        <button className="btn btn-outline-secondary" type="submit">
                            🔍 Search
                        </button>
                    </div>
                </form>
            </div>

            {posts.length === 0 ? (
                <div className='empty-state text-center py-5'>
                    <h3>📝 No blogs found</h3>
                    <p className='text-secondary'>Try adjusting your search or create a new one!</p>
                </div>
            ): (
                <>
                    {posts.map(post => (
                        <div key={post.postId} className="card mb-4 shadow-sm">
                            {post.imageUrl && (
                                <img src={post.imageUrl}
                                     alt={post.title}
                                     className="img-fluid"
                                     style={{maxHeight: '400px', objectFit: 'cover'}}
                                />
                            )}
                            <div className="card-body">
                                <h3 className="card-title fw-bold">{post.title}</h3>

                                <div className="post-meta text-muted mb-3">
                                    <span className="author-info me-3">
                                        👤 {post.authorName || 'Anonymous'}
                                    </span>
                                    <span className="post-time">
                                        🕒 {formatDate(post.createdAt)}
                                    </span>
                                </div>

                                <p className="card-text">{post.body}</p>

                                <div className="card-buttons mt-3">
                                    <div className="d-flex justify-content-end gap-2">
                                        {currentUser && currentUser.id === post.userId && (
                                            <>
                                                <button className="btn btn-outline-info btn-sm" onClick={() => updatePost(post.postId, post.userId)}>
                                                    ✏️ Edit
                                                </button>
                                                <button className="btn btn-outline-danger btn-sm" onClick={() => removePost(post.postId, post.userId)}>
                                                    🗑️ Delete
                                                </button>
                                            </>
                                        )}
                                    </div>
                                </div>
                            </div>
                        </div>
                    ))}

                    <div className="d-flex justify-content-center align-items-center gap-3 mt-5 mb-5">
                        <button
                            className="btn btn-outline-primary"
                            disabled={pageNo === 0}
                            onClick={() => handlePageChange(pageNo - 1)}
                        >
                            &laquo; Previous
                        </button>

                        <span className="fw-bold text-muted">
                            Page {pageNo + 1} of {totalPages}
                        </span>

                        <button
                            className="btn btn-outline-primary"
                            disabled={isLast}
                            onClick={() => handlePageChange(pageNo + 1)}
                        >
                            Next &raquo;
                        </button>
                    </div>
                </>
            )}
        </div>
    )
};

export default MainPage