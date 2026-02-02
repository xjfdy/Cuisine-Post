import React, {useState, useEffect} from "react"
import { allPost, deletePost } from "../services/PostService"; 
import { useNavigate } from "react-router-dom";
import AuthService from "../services/AuthService";

const Mainpage = () => { 

    const [posts, setPost] = useState([]); 
    
    const [pageNo, setPageNo] = useState(0);
    const [pageSize] = useState(5); 
    const [totalPages, setTotalPages] = useState(0);
    const [searchQuery, setSearchQuery] = useState('');
    const [isLast, setIsLast] = useState(false);

    const currentUser = AuthService.getCurrentUser();
    const navigator = useNavigate();

    useEffect(() => {
        getAllPost();
    }, [pageNo, pageSize]) 

    function getAllPost() {
        allPost(pageNo, pageSize, searchQuery).then((response) => {
            console.log("Response data from backend:", response.data);
            
            const content = response.data.content || [];
            setPost(content);
            
            setTotalPages(response.data.totalPages || 0);
            setIsLast(response.data.last || false);
        }).catch(error => {
            console.error("Fetch error:", error);
            setPost([]);
        })
    }

    function handleSearch(e) {
        e.preventDefault();
        setPageNo(0); 
        getAllPost(); 
    }

    function handlePageChange(newPage) {
        if (newPage >= 0 && newPage < totalPages) {
            setPageNo(newPage);
            window.scrollTo(0, 0); 
        }
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
            alert('Please login to edit');
            navigator('/login');
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
            deletePost(id).then(() => {
                getAllPost();
            }).catch(error => {
                console.error('Failed to delete post', error);
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
        if(diffInMinutes < 60) return `${diffInMinutes} minute${diffInMinutes > 1 ? 's' : ''} ago`;
        const diffInHours= Math.floor(diffInMinutes / 60);
        if (diffInHours < 24) return `${diffInHours} hour${diffInHours > 1 ? 's' : ''} ago`;
        const diffInDays = Math.floor(diffInHours / 24);
        if (diffInDays < 7) return `${diffInDays} day${diffInDays > 1 ? 's' : ''} ago`;

        return date.toLocaleDateString('en-US', {
            year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
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

                <form onSubmit={handleSearch}>
                    <div className="input-group">
                        <input 
                            type="text" 
                            className="form-control" 
                            placeholder="Search posts by title..." 
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                        />
                        <button className="btn btn-outline-secondary" type="submit">
                            🔍 Search
                        </button>
                    </div>
                </form>
            </div>

            {!Array.isArray(posts) || posts.length === 0 ? (
                <div className='empty-state text-center py-5'>
                    <h3>📝 No blogs found</h3>
                    <p className='text-secondary'>Try a different search or be the first to post!</p>
                </div>
            ) : (
                <>
                    {posts.map(post => (
                        <div key={post.postId || post.id} className="card mb-4 shadow-sm">
                            {post.imageUrl && (
                                <img src={post.imageUrl} alt={post.title} className="img-fluid" style={{maxHeight: '400px', objectFit: 'cover', width: '100%'}}/>
                            )}
                            
                            <div className="card-body">
                                <h3 className="card-title fw-bold">{post.title}</h3>

                                <div className="post-meta text-muted mb-3">
                                    <span className="author-info me-3">
                                        👤 {post.authorName || 'Anonymous'}
                                    </span>
                                    <span className="post-time">
                                        🕒 {formatDate(post.createdAt || post.createDate)}
                                    </span>
                                </div>

                                <p className="card-text">{post.body}</p>

                                <div className="card-buttons mt-3">
                                    <div className="d-flex justify-content-end gap-2">
                                        {currentUser && currentUser.id === post.userId && (
                                            <>
                                                <button className="btn btn-outline-info btn-sm" onClick={() => updatePost(post.postId || post.id, post.userId)}>
                                                    ✏️ Edit
                                                </button>
                                                <button className="btn btn-outline-danger btn-sm" onClick={() => removePost(post.postId || post.id, post.userId)}>
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
                            disabled={isLast || pageNo >= totalPages - 1}
                            onClick={() => handlePageChange(pageNo + 1)}
                        >
                            Next &raquo;
                        </button>
                    </div>
                </>
            )}
        </div>
    );
};

export default Mainpage;