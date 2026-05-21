output "ec2_public_ip" {
  value = aws_instance.api_server.public_ip
  description = "EC2 public IP for the API server"
}
output "rds_endpoint" {
  value = aws_db_instance.mysql.endpoint
  description = "RDS endpoint"
}
output "s3_bucket_name" {
  value = aws_s3_bucket.digitrans_bucket.bucket
  description = "S3 bucket name"
}
