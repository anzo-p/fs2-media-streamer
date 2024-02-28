import React, { CSSProperties } from 'react';
import { AudioTrack } from '../types/AudioTrack';
import { useAudio } from './PlayerStateProvider';

type AudioTrackCardProps = {
  track: AudioTrack;
  isPlaying: boolean;
};

const cardStyle: CSSProperties = {
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'center',
  width: '100%',
  height: '100px',
  border: '1px solid #ccc',
  padding: '10px',
  margin: '10px 0',
  borderRadius: '5px',
  boxSizing: 'border-box'
};

export const AudioTrackCard: React.FC<AudioTrackCardProps> = ({ track, isPlaying }) => {
  const division = (content: string) => <div style={{ flex: 1, textAlign: 'center' }}>{content}</div>;

  const { setCurrentTrack, play } = useAudio();

  const changeTrack = () => {
    setCurrentTrack(track.url);
    play();
  };

  return (
    <div style={cardStyle}>
      {division(track.title)}
      {division(track.artist)}
      {division(track.album || '')}
      {division(track.genre?.join(', ') || '')}
      <div style={{ flex: 1 }}>
        <button onClick={changeTrack} style={{ width: '100%' }}>
          {isPlaying ? 'Pause' : 'Play'}
        </button>
      </div>
    </div>
  );
};
