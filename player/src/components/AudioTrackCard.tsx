import React, { CSSProperties, useEffect, useState } from 'react';
import { AudioTrack } from '../types/AudioTrack';
import { useAudio } from './PlayerStateProvider';
import { fetchWithTimeout } from '../helpers/fetch';

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

  const getUrl = (trackId: string) => `https://musicbox.anzop.net/tracks/${trackId}/stream`;

  const thisCurrentlyPlaying = () => currentTrack === getUrl(track.trackId) && isPlaying;

  const changeTrack = async () => {
    try {
      const response: Response = await fetchWithTimeout(getUrl(track.trackId));
      setCurrentTrack(response.url);
      thisCurrentlyPlaying() ? pause() : play();
    } catch (error) {
      console.error('Fetch error:', error);
    }
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
