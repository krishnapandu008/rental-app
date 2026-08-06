import React from 'react';
import styles from './VoiceButton.module.scss';

interface VoiceButtonProps {
  isListening: boolean;
  toggleListening: () => void;
  isSupported: boolean;
  error: string | null;
  size?: 'sm' | 'md' | 'lg';
}

const VoiceButton: React.FC<VoiceButtonProps> = ({
  isListening,
  toggleListening,
  isSupported,
  error,
  size = 'md',
}) => {
  if (!isSupported) {
    return (
      <span className={styles.unsupported} title="Voice search not supported">
        🎤
      </span>
    );
  }

  return (
    <div className={styles.voiceContainer}>
      <button
        className={`${styles.voiceBtn} ${isListening ? styles.listening : ''} ${styles[size]}`}
        onClick={toggleListening}
        aria-label={isListening ? 'Stop listening' : 'Start voice search'}
        title={isListening ? 'Stop listening' : 'Click to speak'}
      >
        <span className={styles.micIcon}>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z" />
            <path d="M19 10v2a7 7 0 0 1-14 0v-2" />
            <line x1="12" y1="19" x2="12" y2="23" />
            <line x1="8" y1="23" x2="16" y2="23" />
          </svg>
        </span>
        {isListening && (
          <div className={styles.listeningWave}>
            <span></span>
            <span></span>
            <span></span>
            <span></span>
          </div>
        )}
      </button>
      {error && <span className={styles.errorTooltip}>{error}</span>}
    </div>
  );
};

export default VoiceButton;