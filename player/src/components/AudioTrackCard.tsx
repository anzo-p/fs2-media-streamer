import React, { CSSProperties } from 'react';
import { AudioTrack } from '../types/AudioTrack';
import { useAudio } from './PlayerStateProvider';

type AudioTrackCardProps = {
  track: AudioTrack;
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

export const AudioTrackCard: React.FC<AudioTrackCardProps> = ({ track }) => {
  const { currentTrack, setCurrentTrack, isPlaying, play, pause } = useAudio();

  const getUrl = (trackId: string) => `http://127.0.0.1:8080/tracks/${trackId}/stream`;

  const thisCurrentlyPlaying = () => currentTrack === getUrl(track.trackId) && isPlaying;

  const changeTrack = () => {
    setCurrentTrack(getUrl(track.trackId));
    thisCurrentlyPlaying() ? pause() : play();
  };

  const division = (content: string) => <div style={{ flex: 1, textAlign: 'center' }}>{content}</div>;

  return (
    <div style={cardStyle}>
      {division(track.title)}
      {division(track.artist)}
      {division(track.album || '')}
      {division(track.genre?.join(', ') || '')}
      <div style={{ flex: 1 }}>
        <button onClick={changeTrack} style={{ width: '100%' }}>
          {thisCurrentlyPlaying() ? 'Pause' : 'Play'}
        </button>
      </div>
    </div>
  );
};
