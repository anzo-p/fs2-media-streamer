import React, { useState } from 'react';

export const SearchBar: React.FC<{ onSearch: (query: string) => void }> = ({
  onSearch
}) => {
  const [query, setQuery] = useState('');

  const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    onSearch(query);
  };

  return (
    <div>
      <form
        onSubmit={handleSubmit}
        style={{ display: 'flex', alignItems: 'center' }}
      >
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Search..."
          style={{ flexGrow: 1, marginRight: '8px' }}
        />
        <button
          type="submit"
          style={{
            border: 'none',
            background: 'transparent',
            cursor: 'pointer'
          }}
        >
          🔍
        </button>
      </form>
    </div>
  );
};
