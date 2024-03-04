import { AudioTrack, AudioTrackQuery } from '../types/AudioTrack';

const fetchSampleTracks = async (): Promise<AudioTrack[]> => {
  const response = await fetch('https://musicbox.anzop.net/tracks/search/sample');
  const data: AudioTrack[] = await response.json();
  return data;
};

const searchTracks = async (query: AudioTrackQuery): Promise<AudioTrack[]> => {
  const response = await fetch('https://musicbox.anzop.net/tracks/search/query', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(query)
  });

  if (!response.ok) {
    throw new Error('Network response was not ok');
  }

  const data: AudioTrack[] = await response.json();
  return data;
};

export { fetchSampleTracks, searchTracks };
