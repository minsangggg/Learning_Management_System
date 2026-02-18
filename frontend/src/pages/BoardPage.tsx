import { FormEvent, useEffect, useState } from "react";
import { apiRequest } from "../api/client";

type Board = {
  id: number;
  title: string;
  content: string | null;
  createdAt: string | null;
  updatedAt: string | null;
};

const formatDate = (value?: string | null) => {
  if (!value) return "";
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) return value;
  return parsed.toLocaleString();
};

export default function BoardPage() {
  const [boards, setBoards] = useState<Board[]>([]);
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [editingId, setEditingId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadBoards = () => {
    setLoading(true);
    apiRequest<Board[]>("/api/boards")
      .then((data) => {
        setBoards(data);
        setError(null);
      })
      .catch((err) => setError((err as Error).message))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadBoards();
  }, []);

  const resetForm = () => {
    setTitle("");
    setContent("");
    setEditingId(null);
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!title.trim()) {
      setError("제목을 입력해주세요.");
      return;
    }
    setError(null);
    try {
      if (editingId) {
        await apiRequest<Board>(`/api/boards/${editingId}`, {
          method: "PUT",
          body: JSON.stringify({ title, content }),
        });
      } else {
        await apiRequest<Board>("/api/boards", {
          method: "POST",
          body: JSON.stringify({ title, content }),
        });
      }
      resetForm();
      loadBoards();
    } catch (err) {
      setError((err as Error).message);
    }
  };

  const startEdit = (board: Board) => {
    setEditingId(board.id);
    setTitle(board.title);
    setContent(board.content ?? "");
  };

  const cancelEdit = () => {
    resetForm();
  };

  const handleDelete = async (boardId: number) => {
    const confirmed = window.confirm("삭제하시겠습니까?");
    if (!confirmed) return;
    try {
      await apiRequest<void>(`/api/boards/${boardId}`, { method: "DELETE" });
      if (editingId === boardId) {
        resetForm();
      }
      loadBoards();
    } catch (err) {
      setError((err as Error).message);
    }
  };

  return (
    <main className="page">
      <div className="page-header">
        <h1>게시판</h1>
        <p className="muted">공지와 질문을 공유하는 공간입니다.</p>
      </div>

      {error && <p className="error">{error}</p>}

      <section className="card section">
        <h2>{editingId ? "게시글 수정" : "게시글 작성"}</h2>
        <form className="form-grid" onSubmit={handleSubmit}>
          <label>
            제목
            <input value={title} onChange={(e) => setTitle(e.target.value)} />
          </label>
          <label>
            내용
            <textarea value={content} onChange={(e) => setContent(e.target.value)} rows={4} />
          </label>
          <div className="form-actions">
            <button className="btn btn-primary" type="submit">
              {editingId ? "수정" : "등록"}
            </button>
            {editingId && (
              <button className="btn" type="button" onClick={cancelEdit}>
                취소
              </button>
            )}
          </div>
        </form>
      </section>

      <section className="card section">
        <h2>게시글 목록</h2>
        {loading && <p className="muted">불러오는 중...</p>}
        {!loading && boards.length === 0 ? (
          <p className="muted">등록된 게시글이 없습니다.</p>
        ) : (
          <ul className="card-grid">
            {boards.map((board) => (
              <li key={board.id}>
                <div className="section">
                  <strong>{board.title}</strong>
                  <p className="muted">{formatDate(board.createdAt)}</p>
                </div>
                {board.content && <p>{board.content}</p>}
                {board.updatedAt && (
                  <p className="muted">수정: {formatDate(board.updatedAt)}</p>
                )}
                <div className="form-actions">
                  <button className="btn" type="button" onClick={() => startEdit(board)}>
                    수정
                  </button>
                  <button className="btn btn-danger" type="button" onClick={() => handleDelete(board.id)}>
                    삭제
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </main>
  );
}
