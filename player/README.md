# Hosting from AWS S3

- [x] Create bucket whose name exactly mathces your domain alias in Route53
- [x] Apply public permission to get objects only
- [x] npm run build
- [x] Upload everything from result of npm build, contents of build only
- [x] Apply Static WebHosting, set Index and Error documents
- [x] Create AName in Route53 for a subdomain to pint to that S3 bucket
- [ ] CloudFront for HTTPS...
- [ ] make into a GihHub action CI

That policy

```{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "PublicReadGetObject",
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::<your subdomain for this>/*"
        }
    ]
}
```
