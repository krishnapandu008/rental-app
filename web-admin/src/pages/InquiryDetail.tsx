import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import styles from './InquiryDetail.module.scss';

interface InquiryDetail {
  id: number;
  propertyTitle: string;
  senderName: string;
  senderEmail: string;
  message: string;
  reply: string | null;
  status: string;
  createdAt: string;
  repliedAt: string | null;
}

const InquiryDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [inquiry, setInquiry] = useState<InquiryDetail | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchInquiry = async () => {
      try {
        const res = await api.get(`/inquiries/${id}`);
        setInquiry(res.data);
      } catch (err) {
        console.error('Failed to load inquiry', err);
      } finally {
        setLoading(false);
      }
    };
    fetchInquiry();
  }, [id]);

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (loading) {
    return (
      <div className={styles.container}>
        <div className={styles.emptyState}>
          <span className={styles.emptyIcon}>⏳</span>
          <p>Loading conversation...</p>
        </div>
      </div>
    );
  }

  if (!inquiry) {
    return (
      <div className={styles.container}>
        <div className={styles.emptyState}>
          <span className={styles.emptyIcon}>🔍</span>
          <p>Inquiry not found</p>
        </div>
      </div>
    );
  }

  const getStatusClass = () => {
    switch (inquiry.status?.toLowerCase()) {
      case 'new':
        return styles.new;
      case 'replied':
        return styles.replied;
      case 'read':
        return styles.read;
      default:
        return '';
    }
  };

  return (
    <div className={styles.container}>
      <button className={styles.backButton} onClick={() => navigate(-1)}>
        ← Back
      </button>

      <div className={styles.card}>
        <div className={styles.header}>
          <h1 className={styles.title}>
            Inquiry about {inquiry.propertyTitle}
          </h1>
          <span className={`${styles.statusBadge} ${getStatusClass()}`}>
            {inquiry.status || 'New'}
          </span>
        </div>

        {/* Sender Info */}
        <div className={styles.section}>
          <div className={styles.sectionLabel}>From</div>
          <div className={styles.messageCard}>
            <div className={styles.messageHeader}>
              <span className={styles.senderName}>{inquiry.senderName}</span>
              <span className={styles.messageTime}>
                {formatDate(inquiry.createdAt)}
              </span>
            </div>
            <p className={styles.messageText}>{inquiry.message}</p>
          </div>
        </div>

        {/* Reply Section */}
        {inquiry.reply ? (
          <div className={styles.replySection}>
            <div className={styles.sectionLabel}>Reply</div>
            <div className={`${styles.messageCard} ${styles.replyMessage}`}>
              <div className={styles.messageHeader}>
                <span className={styles.senderName}>Owner</span>
                <span className={styles.messageTime}>
                  {inquiry.repliedAt ? formatDate(inquiry.repliedAt) : ''}
                </span>
              </div>
              <p className={styles.messageText}>{inquiry.reply}</p>
            </div>
          </div>
        ) : (
          <div className={styles.replySection}>
            <div style={{ color: '#6c757d', fontSize: '0.95rem' }}>
              No reply yet
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default InquiryDetail;