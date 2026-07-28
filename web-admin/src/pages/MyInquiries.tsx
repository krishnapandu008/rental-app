import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import styles from './MyInquiries.module.scss';

interface Inquiry {
  id: number;
  propertyId: number;
  propertyTitle: string;
  senderName: string;
  senderEmail: string;
  message: string;
  reply: string;
  status: string;
  createdAt: string;
  repliedAt: string;
  isReceived?: boolean;
  isSent?: boolean;
}

const MyInquiries: React.FC = () => {
  const [inquiries, setInquiries] = useState<Inquiry[]>([]);
  const [loading, setLoading] = useState(true);
  const [replyingTo, setReplyingTo] = useState<number | null>(null);
  const [replyText, setReplyText] = useState('');
  const [sending, setSending] = useState(false);
  const [filter, setFilter] = useState<'all' | 'received' | 'sent'>('all');

  useEffect(() => {
    loadInquiries();
  }, []);

  const loadInquiries = async () => {
    try {
      // Use the new endpoint that returns ALL inquiries
      const res = await api.get('/inquiries/my-all');
      setInquiries(res.data);
    } catch (err) {
      console.error('Failed to load inquiries:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleMarkAsRead = async (inquiryId: number) => {
    try {
      await api.patch(`/inquiries/${inquiryId}/read`);
      setInquiries(prev =>
        prev.map(i => i.id === inquiryId ? { ...i, status: 'READ' } : i)
      );
    } catch (err) {
      console.error('Failed to mark as read:', err);
    }
  };

  const handleReply = async (inquiryId: number) => {
    if (!replyText.trim()) return;
    setSending(true);
    try {
      await api.post(`/inquiries/${inquiryId}/reply`, { reply: replyText });
      setInquiries(prev =>
        prev.map(i =>
          i.id === inquiryId
            ? { ...i, reply: replyText, status: 'REPLIED', repliedAt: new Date().toISOString() }
            : i
        )
      );
      setReplyingTo(null);
      setReplyText('');
    } catch (err) {
      console.error('Failed to send reply:', err);
      alert('Failed to send reply. Please try again.');
    } finally {
      setSending(false);
    }
  };

  const getFilteredInquiries = () => {
    if (filter === 'all') return inquiries;
    if (filter === 'received') return inquiries.filter(i => i.isReceived);
    if (filter === 'sent') return inquiries.filter(i => i.isSent);
    return inquiries;
  };

  const filteredInquiries = getFilteredInquiries();

  if (loading) return <div className={styles.loading}>Loading inquiries...</div>;

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h2 className={styles.title}>📩 My Inquiries</h2>
        <div className={styles.headerRight}>
          <span className={styles.count}>
            {inquiries.filter(i => i.status === 'NEW').length} new
          </span>
        </div>
      </div>

      {/* Filter Tabs */}
      <div className={styles.filterTabs}>
        <button
          className={`${styles.filterTab} ${filter === 'all' ? styles.active : ''}`}
          onClick={() => setFilter('all')}
        >
          All ({inquiries.length})
        </button>
        <button
          className={`${styles.filterTab} ${filter === 'received' ? styles.active : ''}`}
          onClick={() => setFilter('received')}
        >
          Received ({inquiries.filter(i => i.isReceived).length})
        </button>
        <button
          className={`${styles.filterTab} ${filter === 'sent' ? styles.active : ''}`}
          onClick={() => setFilter('sent')}
        >
          Sent ({inquiries.filter(i => i.isSent).length})
        </button>
      </div>

      {filteredInquiries.length === 0 ? (
        <div className={styles.emptyState}>
          <p>No inquiries {filter !== 'all' ? filter : ''}.</p>
        </div>
      ) : (
        <div className={styles.inquiryList}>
          {filteredInquiries.map((inquiry) => (
            <div
              key={inquiry.id}
              className={`${styles.inquiryCard} ${inquiry.status === 'NEW' ? styles.unread : ''}`}
            >
              <div className={styles.inquiryHeader}>
                <div className={styles.propertyInfo}>
                  <Link to={`/inquiry/${inquiry.id}`} className={styles.propertyLink}>
                    <h3>{inquiry.propertyTitle}</h3>
                  </Link>
                  <span className={styles.badge}>{inquiry.status}</span>
                  {inquiry.isReceived && (
                    <span className={styles.typeBadge} title="Received inquiry">
                      📩 Received
                    </span>
                  )}
                  {inquiry.isSent && (
                    <span className={`${styles.typeBadge} ${styles.sentBadge}`} title="Sent inquiry">
                      ✉️ Sent
                    </span>
                  )}
                </div>
                <div className={styles.senderInfo}>
                  <span className={styles.senderName}>{inquiry.senderName}</span>
                  {inquiry.senderEmail && (
                    <span className={styles.senderEmail}>{inquiry.senderEmail}</span>
                  )}
                  <span className={styles.date}>
                    {new Date(inquiry.createdAt).toLocaleDateString()}
                  </span>
                </div>
              </div>

              <div className={styles.messageContent}>
                <p><strong>Message:</strong> {inquiry.message}</p>
              </div>

              {inquiry.reply && (
                <div className={styles.replyContent}>
                  <p><strong>Reply:</strong> {inquiry.reply}</p>
                  <span className={styles.replyDate}>
                    Replied: {new Date(inquiry.repliedAt).toLocaleDateString()}
                  </span>
                </div>
              )}

              <div className={styles.actions}>
                {inquiry.status === 'NEW' && inquiry.isReceived && (
                  <button
                    className={styles.readBtn}
                    onClick={() => handleMarkAsRead(inquiry.id)}
                  >
                    Mark as Read
                  </button>
                )}

                {!inquiry.reply && inquiry.isReceived ? (
                  <button
                    className={styles.replyBtn}
                    onClick={() => setReplyingTo(inquiry.id)}
                  >
                    Reply
                  </button>
                ) : inquiry.reply ? (
                  <span className={styles.repliedBadge}>✅ Replied</span>
                ) : null}

                <Link to={`/inquiry/${inquiry.id}`} className={styles.viewBtn}>
                  👁️ View Conversation
                </Link>
              </div>

              {replyingTo === inquiry.id && (
                <div className={styles.replyForm}>
                  <textarea
                    value={replyText}
                    onChange={(e) => setReplyText(e.target.value)}
                    placeholder="Write your reply..."
                    rows={3}
                    className={styles.replyInput}
                  />
                  <div className={styles.replyActions}>
                    <button
                      className={styles.sendReplyBtn}
                      onClick={() => handleReply(inquiry.id)}
                      disabled={sending || !replyText.trim()}
                    >
                      {sending ? 'Sending...' : 'Send Reply'}
                    </button>
                    <button
                      className={styles.cancelReplyBtn}
                      onClick={() => {
                        setReplyingTo(null);
                        setReplyText('');
                      }}
                    >
                      Cancel
                    </button>
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default MyInquiries;