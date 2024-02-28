export type AudioTrack = {
  trackId: string;
  artist: string;
  genre: string[] | null;
  title: string;
  album: string | null;
  url: string;
};

export type AudioTrackQuery = AudioTrack & {
  page: number;
  length: number;
};
