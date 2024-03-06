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
  const [isWaitingForBackend, setIsWaitingForBackend] = useState(false);
  const [isBufferingStream, setIsBufferingStream] = useState(false);
  const [waitingTime, setWaitingTime] = useState(0);

  const getUrl = (trackId: string) => `https://musicbox.anzop.net/tracks/${trackId}/stream`;

  const thisCurrentlyPlaying = () => currentTrack === getUrl(track.trackId) && isPlaying;

  useEffect(() => {
    let intervalId: NodeJS.Timeout;
    if (isWaitingForBackend || isBufferingStream) {
      intervalId = setInterval(() => {
        setWaitingTime((time) => time + 1);
      }, 1000);
    }
    return () => clearInterval(intervalId);
  }, [isWaitingForBackend, isBufferingStream]);

  const changeTrack = async () => {
    try {
      setIsWaitingForBackend(true);
      const response: Response = await fetchWithTimeout(getUrl(track.trackId));
      setIsWaitingForBackend(false);
      setWaitingTime(0);

      setCurrentTrack(response.url);

      setIsBufferingStream(true);
      thisCurrentlyPlaying() ? pause() : play();
      setIsBufferingStream(false);
    } catch (error) {
      console.error('Fetch error:', error);
      setIsWaitingForBackend(false);
      setIsBufferingStream(false);
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
        {isWaitingForBackend && <div style={{ flex: 1, textAlign: 'center' }}>Waiting for backend.. {waitingTime}</div>}
        {isBufferingStream && <div style={{ flex: 1, textAlign: 'center' }}>Buffering.. {waitingTime} </div>}
      </div>
    </div>
  );
};
