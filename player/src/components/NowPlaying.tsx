import React from 'react';

export const NowPlaying: React.FC<{ url: string }> = ({ url }) => {
  return (
    <audio controls autoPlay src={url}>
      Your browser does not support the audio element.
    </audio>
  );
};
