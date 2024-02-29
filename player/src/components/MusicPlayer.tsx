import React, { useEffect, useState } from 'react';

import { AudioTrackList } from './AudioTrackList';
import { NowPlaying } from './NowPlaying';
import { SearchBar } from './SearchBar';
import { fetchSampleTracks } from '../services/musicApi';
import { AudioTrack } from '../types/AudioTrack';

export const MusicPlayer: React.FC = () => {
  const [tracks, setTracks] = useState<AudioTrack[]>([]);

  useEffect(() => {
    const initFetch = async () => {
      const tracks = await fetchSampleTracks();
      setTracks(tracks);
    };

    initFetch();
  }, []);

  return (
    <div>
      <h1>Music Player</h1>
      <SearchBar
        onSearch={function (query: string): void {
          throw new Error('Function not implemented.');
        }}
      />
      <AudioTrackList tracks={tracks} />
      <NowPlaying />
    </div>
  );
};
