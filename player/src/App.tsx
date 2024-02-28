import { MusicPlayer } from './components/MusicPlayer';
import { PlayerStateProvider } from './components/PlayerStateProvider';
import './App.css';

function App() {
  return (
    <PlayerStateProvider>
      <MusicPlayer />
    </PlayerStateProvider>
  );
}

export default App;
