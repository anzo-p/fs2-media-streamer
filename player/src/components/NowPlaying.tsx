import { FC, useEffect, useRef } from 'react';
import { useAudio } from './PlayerStateProvider';

export const NowPlaying: FC = () => {
  const { currentTrack, isPlaying } = useAudio();
  const audioRef = useRef<HTMLAudioElement>(null);

  useEffect(() => {
    if (audioRef.current) {
      isPlaying ? audioRef.current.play() : audioRef.current.pause();
    }
  }, [isPlaying]);

  return (
    <audio ref={audioRef} controls autoPlay src={currentTrack}>
      Your browser does not support the audio element.
    </audio>
  );
};
