import { createContext, useContext, useRef, useState, FC, ReactNode, useEffect } from 'react';

type PlayerStateContextType = {
  currentTrack: string;
  setCurrentTrack: (url: string) => void;
  isPlaying: boolean;
  play: () => void;
  pause: () => void;
};

const PlayerStateContext = createContext<PlayerStateContextType | undefined>(undefined);

export const useAudio = () => {
  const context = useContext(PlayerStateContext);
  if (!context) {
    throw new Error('useAudio must be used within an AudioProvider');
  }
  return context;
};

export const PlayerStateProvider: FC<{ children: ReactNode }> = ({ children }) => {
  const audioRef = useRef<HTMLAudioElement>(new Audio());

  const [currentTrack, setCurrentTrack] = useState('');
  const [isPlaying, setIsPlaying] = useState(false);

  const play = () => setIsPlaying(true);
  const pause = () => setIsPlaying(false);

  useEffect(() => {
    const playPromise = audioRef.current.play();
    if (playPromise !== undefined) {
      playPromise.catch((error) => {
        console.error('Error playing audio:', error);
      });
    }
  }, [currentTrack]);

  const playAudio = () => {
    if (currentTrack) {
      audioRef.current.src = currentTrack;
      audioRef.current.play();
    }
  };

  return (
    <PlayerStateContext.Provider value={{ currentTrack, setCurrentTrack, isPlaying, play, pause }}>
      {children}
    </PlayerStateContext.Provider>
  );
};
