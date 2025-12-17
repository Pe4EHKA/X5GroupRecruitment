'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import { Box, Button, Stack, Typography, Alert, Chip } from '@mui/material';
import { FiberManualRecord, RestartAlt, Stop } from '@mui/icons-material';

interface VideoRecorderProps {
  onRecordingComplete: (file: File) => void;
  maxDurationSeconds?: number;
  disabled?: boolean;
}

const formatDuration = (seconds: number) => {
  const mins = Math.floor(seconds / 60)
    .toString()
    .padStart(2, '0');
  const secs = Math.floor(seconds % 60)
    .toString()
    .padStart(2, '0');
  return `${mins}:${secs}`;
};

export default function VideoRecorder({ onRecordingComplete, maxDurationSeconds = 300, disabled }: VideoRecorderProps) {
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const mediaRecorderRef = useRef<MediaRecorder | null>(null);
  const mediaStreamRef = useRef<MediaStream | null>(null);
  const chunksRef = useRef<Blob[]>([]);

  const [isRecording, setIsRecording] = useState(false);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [elapsedSeconds, setElapsedSeconds] = useState(0);
  const [error, setError] = useState<string | null>(null);

  const stopRecorder = useCallback(() => {
    if (mediaRecorderRef.current && mediaRecorderRef.current.state !== 'inactive') {
      mediaRecorderRef.current.stop();
    }
    mediaStreamRef.current?.getTracks().forEach((t) => t.stop());
    setIsRecording(false);
  }, []);

  useEffect(() => {
    if (videoRef.current) {
      if (mediaStreamRef.current) {
        videoRef.current.srcObject = mediaStreamRef.current;
        void videoRef.current.play();
      } else if (previewUrl) {
        videoRef.current.srcObject = null;
        videoRef.current.src = previewUrl;
      }
    }
  }, [previewUrl]);

  useEffect(() => {
    if (!isRecording) return undefined;

    const interval = setInterval(() => {
      setElapsedSeconds((prev) => {
        const next = prev + 1;
        if (maxDurationSeconds && next >= maxDurationSeconds) {
          stopRecorder();
        }
        return next;
      });
    }, 1000);

    return () => clearInterval(interval);
  }, [isRecording, maxDurationSeconds, stopRecorder]);

  useEffect(() => {
    return () => {
      mediaStreamRef.current?.getTracks().forEach((t) => t.stop());
      if (previewUrl) {
        URL.revokeObjectURL(previewUrl);
      }
    };
  }, [previewUrl]);

  const startRecording = async () => {
    try {
      setError(null);
      setElapsedSeconds(0);
      if (previewUrl) {
        URL.revokeObjectURL(previewUrl);
        setPreviewUrl(null);
      }

      const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
      mediaStreamRef.current = stream;

      const mediaRecorder = new MediaRecorder(stream, { mimeType: 'video/webm' });
      chunksRef.current = [];

      mediaRecorder.ondataavailable = (event: BlobEvent) => {
        if (event.data.size > 0) {
          chunksRef.current.push(event.data);
        }
      };

      mediaRecorder.onstop = () => {
        const blob = new Blob(chunksRef.current, { type: 'video/webm' });
        const file = new File([blob], `video-presentation-${Date.now()}.webm`, { type: 'video/webm' });
        const url = URL.createObjectURL(blob);
        setPreviewUrl(url);
        onRecordingComplete(file);
        mediaStreamRef.current?.getTracks().forEach((t) => t.stop());
        mediaStreamRef.current = null;
        setIsRecording(false);
      };

      mediaRecorderRef.current = mediaRecorder;
      mediaRecorder.start();
      setIsRecording(true);
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
        void videoRef.current.play();
      }
    } catch (err) {
      setError('Не удалось получить доступ к камере или микрофону. Проверьте разрешения в браузере.');
      console.error(err);
    }
  };

  const stopRecording = () => {
    stopRecorder();
  };

  const resetRecording = () => {
    if (previewUrl) {
      URL.revokeObjectURL(previewUrl);
      setPreviewUrl(null);
    }
    setElapsedSeconds(0);
    chunksRef.current = [];
  };

  return (
    <Stack spacing={2}>
      <Box
        sx={{
          borderRadius: 2,
          overflow: 'hidden',
          border: '1px solid',
          borderColor: 'divider',
          position: 'relative',
        }}
      >
        <video ref={videoRef} controls={!isRecording} autoPlay muted style={{ width: '100%', display: 'block' }} />
        {isRecording && (
          <Chip
            label={`Запись ${formatDuration(elapsedSeconds)}`}
            color="error"
            icon={<FiberManualRecord />}
            sx={{ position: 'absolute', top: 8, left: 8 }}
          />
        )}
      </Box>

      {error && <Alert severity="error">{error}</Alert>}
      {previewUrl && !isRecording && (
        <Alert severity="info">Запись готова. Нажмите «Загрузить», чтобы прикрепить видео к заявке.</Alert>
      )}

      <Stack direction="row" spacing={2}>
        <Button
          variant="contained"
          startIcon={<FiberManualRecord />}
          color="error"
          onClick={startRecording}
          disabled={isRecording || disabled}
        >
          Начать запись
        </Button>
        <Button
          variant="outlined"
          startIcon={<Stop />}
          onClick={stopRecording}
          disabled={!isRecording}
          color="inherit"
        >
          Остановить
        </Button>
        <Button
          variant="text"
          startIcon={<RestartAlt />}
          onClick={resetRecording}
          disabled={isRecording}
        >
          Перезаписать
        </Button>
        <Typography variant="body2" color="text.secondary" sx={{ ml: 'auto', alignSelf: 'center' }}>
          Лимит записи: {Math.floor(maxDurationSeconds / 60)} мин
        </Typography>
      </Stack>
    </Stack>
  );
}
