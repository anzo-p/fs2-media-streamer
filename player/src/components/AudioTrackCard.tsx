import React, { CSSProperties } from 'react';
import { AudioTrack } from '../types/AudioTrack';

type AudioTrackCardProps = {
  track: AudioTrack;
  onPlayPauseClick: () => void;
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

const division = (content: string) => (
  <div style={{ flex: 1, textAlign: 'center' }}>{content}</div>
);

export const AudioTrackCard: React.FC<AudioTrackCardProps> = ({
  track,
  onPlayPauseClick,
  isPlaying
}) => {
  return (
    <div style={cardStyle}>
      {division(track.title)}
      {division(track.artist)}
      {division(track.album || '')}
      {division(track.genre?.join(', ') || '')}
      <div style={{ flex: 1 }}>
        <button onClick={onPlayPauseClick} style={{ width: '100%' }}>
          {isPlaying ? 'Pause' : 'Play'}
        </button>
      </div>
    </div>
  );
};
