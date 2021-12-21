# CompressionTests

## Sample testing data:
[https://drive.google.com/open?id=1egzZKkV305LX7jt2Xxe0Y-DfYkkozDa_](https://drive.google.com/open?id=1egzZKkV305LX7jt2Xxe0Y-DfYkkozDa_)

## Usage
This program loads uncompressed chunk data into memory to avoid file I/O affecting results. Expect about 4GB of memory per ~50 full regions.

`java -jar -Xmx8192m -Xms8192m compression-tests-1.0.0.jar /path/to/dir/vanilla_testing/world/region`

## Build dictionaries
### Zlib dictionary
Using this library developed by Cloudflare: https://github.com/vkrasnov/dictator

`go run cmd/dictator.go -in /path/to/dir/vanilla_training/world/region/rawchunks -out paper.zlib.dict -l 9`
### Zstd dictionary
Using the Zstd library: https://github.com/facebook/zstd/releases

`./zstd --train-fastcover -r /path/to/dir/vanilla_training/world/region/rawchunks/ -o paper.zstd.dict`
