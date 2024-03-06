export const fetchWithTimeout = (
  resource: RequestInfo,
  options: RequestInit = {},
  timeout: number = 10000
): Promise<Response> => {
  let timeoutId: NodeJS.Timeout;
  const timeoutPromise = new Promise<never>((_, reject) => {
    timeoutId = setTimeout(() => reject(new Error('Request timeout')), timeout);
  });

  return Promise.race([fetch(resource, options), timeoutPromise]).then((response) => {
    clearTimeout(timeoutId);
    return response;
  });
};
