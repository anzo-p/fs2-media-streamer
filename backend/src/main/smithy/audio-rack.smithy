$version: "2"

namespace net.anzop.audiostreamer

use alloy#simpleRestJson

@simpleRestJson
service AudioRackService {
    version: "2021-02-21",
    operations: [AddTrackMetadata, UploadTrack, ListTracks, DownloadTrack]
}

@http(method: "POST", uri: "/tracks", code: 201)
operation AddTrackMetadata {
    input: AddTrackMetadataInput
    output: TrackMetadataOutput
}

list GenreList {
    member: String
}

structure AddTrackMetadataInput {
    album: String,
    @required
    artist: String,
    bitrate: Integer,
    @required
    duration: Integer,
    @required
    fileSize: Integer,
    @required
    format: String,
    @required
    genre: GenreList,
    @required
    title: String,
    year: Integer,
}

structure TrackMetadataOutput {
    album: String,
    @required
    artist: String,
    bitrate: Integer,
    @required
    duration: Integer,
    @required
    fileSize: Integer,
    @required
    format: String,
    @required
    genre: GenreList,
    @required
    title: String,
    @required
    trackId: String,
    year: Integer,
}

list TrackMetadataList {
    member: TrackMetadataOutput
}

@http(method: "POST", uri: "/tracks/{trackId}/file", code: 201)
operation UploadTrack {
    input: UploadTrackInput
}

structure UploadTrackInput {
    @required
    @httpLabel
    trackId: String,

    @required
    track: Blob
}

@http(method: "GET", uri: "/tracks", code: 200)
@readonly
operation ListTracks {
    input: ListTracksInput
    output: ListTracksOutput
}

structure ListTracksInput {
    @httpQuery("offset")
    offset: Integer,
    @httpQuery("limit")
    limit: Integer,
}

structure ListTracksOutput {
    tracks: TrackMetadataList
}

@http(method: "GET", uri: "/tracks/{trackId}", code: 200)
@readonly
operation DownloadTrack {
    input: DownloadTrackInput
    output: DownloadTrackOutput
}

structure DownloadTrackInput {
    @required
    @httpLabel
    trackId: String
}

structure DownloadTrackOutput {
    trackData: Blob
}
