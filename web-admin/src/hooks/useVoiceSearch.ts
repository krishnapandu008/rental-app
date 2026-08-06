import { useState, useCallback, useEffect, useRef } from 'react';

interface UseVoiceSearchProps {
  onResult?: (text: string) => void;
  onError?: (error: string) => void;
  lang?: string;
  timeout?: number;
}

interface UseVoiceSearchReturn {
  isListening: boolean;
  transcript: string;
  startListening: () => void;
  stopListening: () => void;
  toggleListening: () => void;
  isSupported: boolean;
  error: string | null;
}

export const useVoiceSearch = ({
  onResult,
  onError,
  lang = 'en-IN',
  timeout = 10000,
}: UseVoiceSearchProps = {}): UseVoiceSearchReturn => {
  const [isListening, setIsListening] = useState(false);
  const [transcript, setTranscript] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [recognition, setRecognition] = useState<any>(null);
  const [isSupported, setIsSupported] = useState(false);
  const timeoutRef = useRef<number | null>(null);

  useEffect(() => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    
    if (SpeechRecognition) {
      const recognitionInstance = new SpeechRecognition();
      recognitionInstance.lang = lang;
      recognitionInstance.continuous = false;
      recognitionInstance.interimResults = true;
      recognitionInstance.maxAlternatives = 1;
      
      recognitionInstance.onresult = (event: any) => {
        const results = event.results;
        const lastResult = results[results.length - 1];
        const transcriptText = lastResult[0].transcript;
        
        setTranscript(transcriptText);
        
        if (lastResult.isFinal) {
          if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
            timeoutRef.current = null;
          }
          onResult?.(transcriptText);
          setIsListening(false);
        }
      };
      
      recognitionInstance.onerror = (event: any) => {
        const errorMsg = event.error;
        console.error('Speech recognition error:', errorMsg);
        setError(errorMsg);
        onError?.(errorMsg);
        setIsListening(false);
        if (timeoutRef.current) {
          clearTimeout(timeoutRef.current);
          timeoutRef.current = null;
        }
      };
      
      recognitionInstance.onend = () => {
        setIsListening(false);
        if (timeoutRef.current) {
          clearTimeout(timeoutRef.current);
          timeoutRef.current = null;
        }
      };
      
      setRecognition(recognitionInstance);
      setIsSupported(true);
    } else {
      setIsSupported(false);
      setError('Speech recognition is not supported in this browser');
    }
    
    return () => {
      if (recognition) {
        try {
          recognition.abort();
        } catch (e) {
          // Ignore
        }
      }
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
        timeoutRef.current = null;
      }
    };
  }, [lang]); // ✅ FIXED: Removed onResult and onError from dependencies

  const startListening = useCallback(() => {
    if (!recognition) return;
    
    setError(null);
    setTranscript('');
    
    try {
      recognition.start();
      setIsListening(true);
      
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
      timeoutRef.current = window.setTimeout(() => {
        try {
          recognition.stop();
          setError('Voice search timed out. Please try again.');
          onError?.('Voice search timed out');
        } catch (e) {
          // Ignore
        }
        setIsListening(false);
        timeoutRef.current = null;
      }, timeout);
      
    } catch (err: any) {
      setError(err.message);
      setIsListening(false);
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
        timeoutRef.current = null;
      }
    }
  }, [recognition, timeout, onError]);

  const stopListening = useCallback(() => {
    if (!recognition) return;
    
    try {
      recognition.stop();
      setIsListening(false);
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
        timeoutRef.current = null;
      }
    } catch (err) {
      // Ignore
    }
  }, [recognition]);

  const toggleListening = useCallback(() => {
    if (isListening) {
      stopListening();
    } else {
      startListening();
    }
  }, [isListening, startListening, stopListening]);

  return {
    isListening,
    transcript,
    startListening,
    stopListening,
    toggleListening,
    isSupported,
    error,
  };
};

declare global {
  interface Window {
    SpeechRecognition: any;
    webkitSpeechRecognition: any;
  }
}