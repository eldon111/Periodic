# AWS Security Setup Guide

## 🔐 Securing Your AWS Credentials

This project uses Amazon Bedrock AI services and requires AWS credentials. Follow this guide to set
up your credentials securely.

## ⚠️ Security Best Practices

### 1. Never Commit Credentials to Version Control

- AWS credentials are stored in `local.properties` which is gitignored
- Never hardcode credentials in source code
- Never commit `local.properties` to git

### 2. Use IAM Users with Minimal Permissions

Create an IAM user specifically for this app with only the required permissions:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "bedrock:InvokeModel"
            ],
            "Resource": [
                "arn:aws:bedrock:*:*:model/amazon.nova-lite-v1:0"
            ]
        }
    ]
}
```

### 3. Regularly Rotate Access Keys

- Rotate your AWS access keys every 90 days
- Delete unused access keys
- Monitor AWS CloudTrail for unusual activity

## 🚀 Setup Instructions

### Step 1: Create AWS IAM User

1. Go to AWS IAM Console
2. Create a new user for this application
3. Attach the minimal policy shown above
4. Generate access keys for the user

### Step 2: Configure Local Credentials

1. Copy `local.properties.template` to `local.properties`
2. Fill in your AWS credentials:
   ```properties
   aws.region=us-east-4
   aws.access_key_id=YOUR_ACCESS_KEY_ID
   aws.secret_access_key=YOUR_SECRET_ACCESS_KEY
   ```

### Step 3: Verify Setup

1. Build the project
2. Run the app
3. Test AI functionality

## 🛡️ Additional Security Measures

### For Production Apps

Consider these additional security measures for production:

1. **Use AWS STS Temporary Credentials**
    - Implement credential refresh mechanism
    - Use shorter-lived tokens

2. **Implement Certificate Pinning**
    - Pin AWS API certificates
    - Prevent man-in-the-middle attacks

3. **Use Android Keystore**
    - Store credentials in Android's secure hardware
    - Encrypt sensitive data at rest

4. **Implement Credential Validation**
    - Validate credentials before use
    - Handle credential expiration gracefully

### Environment-Specific Configuration

For different environments (dev, staging, prod), consider:

- Using different AWS accounts
- Implementing environment-specific IAM policies
- Using AWS Parameter Store or Secrets Manager

## 🚨 What to Do If Credentials Are Compromised

1. **Immediately disable the compromised access key** in AWS IAM
2. **Generate new access keys**
3. **Update local.properties** with new credentials
4. **Review AWS CloudTrail logs** for unauthorized usage
5. **Consider rotating all related credentials**

## 📞 Support

If you need help with AWS setup:

- Check AWS documentation: https://docs.aws.amazon.com/bedrock/
- Review IAM best practices: https://docs.aws.amazon.com/IAM/latest/UserGuide/best-practices.html
