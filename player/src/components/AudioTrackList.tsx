import React, { CSSProperties } from 'react';
import { AudioTrackCard } from './AudioTrackCard';
import { AudioTrack } from '../types/AudioTrack';

type AudioTrackListProps = {
  tracks: AudioTrack[];
  onPlayPauseClick: (id: string) => void;
};

const listStyle: CSSProperties = {
  display: 'flex',
  flexDirection: 'column',
  justifyContent: 'space-between',
  alignItems: 'center',
  width: '100%',
  border: '1px solid #ccc',
  padding: '10px',
  margin: '10px 0',
  borderRadius: '5px',
  boxSizing: 'border-box'
};

export const AudioTrackList: React.FC<AudioTrackListProps> = ({
  tracks,
  onPlayPauseClick
}) => {
  return (
    <div style={listStyle}>
      {tracks.map((track) => (
        <AudioTrackCard key={track.trackId} track={track} isPlaying={false} />
      ))}
    </div>
  );
};
