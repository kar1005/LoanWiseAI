#!/usr/bin/env python3
# Document Processor for Loan Application Validation
# This script processes loan documents, extracts information, and validates them

import os
import sys
import json
import re
import argparse
import logging
from datetime import datetime
import tempfile
import requests
import cv2
import numpy as np
import pandas as pd
import pytesseract
from PIL import Image
import pdf2image
import pymongo
import cloudinary
import cloudinary.uploader
import cloudinary.api
from cloudinary.exceptions import NotFound

# Set up logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('document_processor.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger('doc_processor')

# Document patterns for validation
PATTERNS = {
    'aadhaar': r'[2-9]{1}[0-9]{3}\s?[0-9]{4}\s?[0-9]{4}',
    'pan': r'[A-Z]{5}[0-9]{4}[A-Z]{1}',
    'mobile': r'(?<!\d)((?:\+91)?[6-9]\d{9})(?!\d)',
    'email': r'[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}',
    'date': r'\d{2}[/-]\d{2}[/-]\d{2,4}|\d{2}\s(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s\d{2,4}'
}

# Document keywords for identification
DOCUMENT_KEYWORDS = {
    'aadhaar': ['aadhaar', 'unique identification', 'आधार', 'uid', 'identification auth', 'govt. of india'],
    'pan': ['income tax', 'permanent account', 'pan', 'dept of income', 'आयकर विभाग'],
    'bank_statement': ['statement', 'account', 'transaction', 'balance', 'deposit', 'withdrawal', 'bank'],
    'itr': ['income tax return', 'itr', 'assessment year', 'tax return', 'form 16', 'tax department']
}

class DocumentProcessor:
    def __init__(self, cloudinary_config, mongodb_config=None):
        """
        Initialize the document processor with configurations
        
        Args:
            cloudinary_config (dict): Cloudinary configuration
            mongodb_config (dict): MongoDB configuration
        """
        # Configure Cloudinary
        cloudinary.config(
            cloud_name=cloudinary_config['cloud_name'],
            api_key=cloudinary_config['api_key'],
            api_secret=cloudinary_config['api_secret']
        )
        
        # Configure MongoDB if provided
        self.db = None
        if mongodb_config:
            try:
                client = pymongo.MongoClient(mongodb_config['connection_string'])
                self.db = client[mongodb_config['database']]
                logger.info("Connected to MongoDB successfully")
            except Exception as e:
                logger.error(f"Error connecting to MongoDB: {e}")
    
    def download_document(self, public_id, download_path):
        """
        Download a document from Cloudinary
        
        Args:
            public_id (str): Cloudinary public ID of the document
            download_path (str): Path to save the downloaded document
            
        Returns:
            str: Path to the downloaded file or None if failed
        """
        try:
            # Get file info from Cloudinary
            file_info = cloudinary.api.resource(public_id)
            file_url = file_info.get('secure_url')
            
            if not file_url:
                logger.error(f"Could not get URL for public_id: {public_id}")
                return None
            
            # Download the file
            response = requests.get(file_url)
            if response.status_code != 200:
                logger.error(f"Failed to download file: {response.status_code}")
                return None
                
            # Save the file
            with open(download_path, 'wb') as f:
                f.write(response.content)
                
            logger.info(f"Downloaded document {public_id} to {download_path}")
            return download_path
        except NotFound:
            logger.error(f"Document {public_id} not found in Cloudinary")
            return None
        except Exception as e:
            logger.error(f"Error downloading document {public_id}: {e}")
            return None
    
    def get_document_type(self, text):
        """
        Determine document type based on content
        
        Args:
            text (str): Extracted text from document
            
        Returns:
            str: Document type or 'unknown'
        """
        text = text.lower()
        
        for doc_type, keywords in DOCUMENT_KEYWORDS.items():
            for keyword in keywords:
                if keyword in text:
                    return doc_type
        
        return "unknown"
    
    def extract_text_from_image(self, image_path):
        """
        Extract text from image using OCR
        
        Args:
            image_path (str): Path to the image file
            
        Returns:
            str: Extracted text
        """
        try:
            image = cv2.imread(image_path)
            if image is None:
                logger.error(f"Failed to read image: {image_path}")
                return ""
                
            # Image preprocessing for better OCR
            gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
            thresh = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY | cv2.THRESH_OTSU)[1]
            
            # OCR with pytesseract
            text = pytesseract.image_to_string(thresh, lang='eng')
            return text
        except Exception as e:
            logger.error(f"Error extracting text from image: {e}")
            return ""
    
    def extract_text_from_pdf(self, pdf_path):
        """
        Extract text from PDF by converting to images and using OCR
        
        Args:
            pdf_path (str): Path to the PDF file
            
        Returns:
            str: Extracted text
        """
        try:
            # Convert PDF to images
            images = pdf2image.convert_from_path(pdf_path)
            
            # Extract text from each page
            text = ""
            for image in images:
                # Save the image temporarily
                with tempfile.NamedTemporaryFile(suffix='.jpg', delete=False) as temp:
                    image.save(temp.name, 'JPEG')
                    temp_path = temp.name
                
                # Extract text from the image
                page_text = self.extract_text_from_image(temp_path)
                text += page_text + "\n\n"
                
                # Remove temporary file
                os.unlink(temp_path)
            
            return text
        except Exception as e:
            logger.error(f"Error extracting text from PDF: {e}")
            return ""
    
    def extract_text(self, file_path):
        """
        Extract text from a file (PDF or image)
        
        Args:
            file_path (str): Path to the file
            
        Returns:
            str: Extracted text
        """
        _, ext = os.path.splitext(file_path)
        ext = ext.lower()
        
        if ext in ['.pdf']:
            return self.extract_text_from_pdf(file_path)
        elif ext in ['.jpg', '.jpeg', '.png', '.tiff', '.tif', '.bmp']:
            return self.extract_text_from_image(file_path)
        else:
            logger.error(f"Unsupported file format: {ext}")
            return ""
    
    def validate_aadhaar(self, aadhaar_number):
        """
        Validate Aadhaar number using Verhoeff algorithm
        
        Args:
            aadhaar_number (str): Aadhaar number to validate
            
        Returns:
            bool: True if valid, False otherwise
        """
        # Remove spaces and check length
        aadhaar = re.sub(r'\s', '', aadhaar_number)
        if len(aadhaar) != 12 or not aadhaar.isdigit():
            return False
            
        # Simple check: Aadhaar shouldn't start with 0 or 1
        if aadhaar[0] in ['0', '1']:
            return False
            
        return True
    
    def validate_pan(self, pan_number):
        """
        Validate PAN card number
        
        Args:
            pan_number (str): PAN number to validate
            
        Returns:
            bool: True if valid, False otherwise
        """
        # Check format: 5 letters + 4 digits + 1 letter
        pan_pattern = r'^[A-Z]{5}[0-9]{4}[A-Z]{1}$'
        if not re.match(pan_pattern, pan_number):
            return False
            
        return True
    
    def extract_aadhaar_details(self, text):
        """
        Extract details from Aadhaar card
        
        Args:
            text (str): Extracted text from Aadhaar card
            
        Returns:
            dict: Extracted details
        """
        details = {'document_type': 'aadhaar', 'is_valid': False}
        
        # Extract Aadhaar number
        aadhaar_matches = re.findall(PATTERNS['aadhaar'], text)
        if aadhaar_matches:
            aadhaar_number = re.sub(r'\s', '', aadhaar_matches[0])
            details['aadhaar_number'] = aadhaar_number
            details['is_valid'] = self.validate_aadhaar(aadhaar_number)
        
        # Extract DOB/Age
        dob_patterns = [
            r'DOB\s*:\s*(\d{2}/\d{2}/\d{4})',
            r'Date of Birth\s*:\s*(\d{2}/\d{2}/\d{4})',
            r'जन्म तिथि\s*:\s*(\d{2}/\d{2}/\d{4})',
            r'(\d{2}/\d{2}/\d{4})'
        ]
        
        for pattern in dob_patterns:
            dob_match = re.search(pattern, text)
            if dob_match:
                dob = dob_match.group(1)
                details['dob'] = dob
                
                # Calculate age from DOB
                try:
                    dob_date = datetime.strptime(dob, '%d/%m/%Y')
                    today = datetime.today()
                    age = today.year - dob_date.year - ((today.month, today.day) < (dob_date.month, dob_date.day))
                    details['age'] = age
                except:
                    pass
                
                break
        
        # Extract name
        name_patterns = [
            r'Name\s*:\s*([A-Za-z\s]+)',
            r'नाम\s*:\s*([A-Za-z\s]+)'
        ]
        
        for pattern in name_patterns:
            name_match = re.search(pattern, text)
            if name_match:
                name = name_match.group(1).strip()
                details['name'] = name
                break
                
        return details
    
    def extract_pan_details(self, text):
        """
        Extract details from PAN card
        
        Args:
            text (str): Extracted text from PAN card
            
        Returns:
            dict: Extracted details
        """
        details = {'document_type': 'pan', 'is_valid': False}
        
        # Extract PAN number
        pan_matches = re.findall(PATTERNS['pan'], text)
        if pan_matches:
            pan_number = pan_matches[0]
            details['pan_number'] = pan_number
            details['is_valid'] = self.validate_pan(pan_number)
        
        # Extract name
        name_patterns = [
            r'Name\s*:\s*([A-Za-z\s]+)',
            r'नाम\s*:\s*([A-Za-z\s]+)'
        ]
        
        for pattern in name_patterns:
            name_match = re.search(pattern, text)
            if name_match:
                name = name_match.group(1).strip()
                details['name'] = name
                break
        
        # Extract DOB (if available)
        dob_patterns = [
            r'Date of Birth\s*:\s*(\d{2}/\d{2}/\d{4})',
            r'DOB\s*:\s*(\d{2}/\d{2}/\d{4})',
            r'(\d{2}/\d{2}/\d{4})'
        ]
        
        for pattern in dob_patterns:
            dob_match = re.search(pattern, text)
            if dob_match:
                dob = dob_match.group(1)
                details['dob'] = dob
                break
                
        return details
    
    def analyze_bank_statement(self, text):
        """
        Analyze bank statement to extract financial information
        
        Args:
            text (str): Extracted text from bank statement
            
        Returns:
            dict: Extracted financial details
        """
        details = {'document_type': 'bank_statement', 'is_valid': False}
        
        # Check if it's a valid bank statement
        if any(keyword in text.lower() for keyword in DOCUMENT_KEYWORDS['bank_statement']):
            details['is_valid'] = True
        
        # Extract average balance and transactions
        balance_pattern = r'(?:closing|ending|available)\s+balance\s*(?::|is|of|rs|inr|₹)?\s*([\d,]+\.?\d*)'
        balance_matches = re.findall(balance_pattern, text.lower())
        
        if balance_matches:
            try:
                # Extract the last balance (most recent)
                closing_balance = float(re.sub(r'[,₹]', '', balance_matches[-1]))
                details['closing_balance'] = closing_balance
            except:
                pass
        
        # Extract total credits (deposits)
        credit_pattern = r'(?:total|sum of)\s+(?:credit|deposit)s?\s*(?::|is|of|rs|inr|₹)?\s*([\d,]+\.?\d*)'
        credit_matches = re.findall(credit_pattern, text.lower())
        
        if credit_matches:
            try:
                total_credits = float(re.sub(r'[,₹]', '', credit_matches[0]))
                details['total_credits'] = total_credits
            except:
                pass
        
        # Extract total debits (withdrawals)
        debit_pattern = r'(?:total|sum of)\s+(?:debit|withdrawal)s?\s*(?::|is|of|rs|inr|₹)?\s*([\d,]+\.?\d*)'
        debit_matches = re.findall(debit_pattern, text.lower())
        
        if debit_matches:
            try:
                total_debits = float(re.sub(r'[,₹]', '', debit_matches[0]))
                details['total_debits'] = total_debits
            except:
                pass
        
        # Extract loan EMIs or recurring payments (potential existing loans)
        emi_patterns = [
            r'(?:emi|loan)\s+(?:payment|repayment)\s*(?::|to|of|for)?\s*(\w+)',
            r'(?:emi|loan)\s+(?:payment|repayment)\s*(?::|to|of|for)?\s*[^\d]*([\d,]+\.?\d*)'
        ]
        
        existing_loans = []
        for pattern in emi_patterns:
            emi_matches = re.findall(pattern, text.lower())
            for match in emi_matches:
                if match and match not in existing_loans:
                    existing_loans.append(match)
        
        if existing_loans:
            details['potential_loan_emis'] = existing_loans
        
        return details
    
    def analyze_itr(self, text):
        """
        Analyze Income Tax Return document to extract income information
        
        Args:
            text (str): Extracted text from ITR document
            
        Returns:
            dict: Extracted income details
        """
        details = {'document_type': 'itr', 'is_valid': False}
        
        # Check if it's a valid ITR document
        if any(keyword in text.lower() for keyword in DOCUMENT_KEYWORDS['itr']):
            details['is_valid'] = True
        
        # Extract gross total income
        income_patterns = [
            r'(?:gross total income|gti)\s*(?::|is|of|rs|inr|₹)?\s*([\d,]+\.?\d*)',
            r'(?:total income|taxable income)\s*(?::|is|of|rs|inr|₹)?\s*([\d,]+\.?\d*)'
        ]
        
        for pattern in income_patterns:
            income_matches = re.findall(pattern, text.lower())
            if income_matches:
                try:
                    income = float(re.sub(r'[,₹]', '', income_matches[0]))
                    details['annual_income'] = income
                    break
                except:
                    pass
        
        # Extract assessment year
        ay_pattern = r'(?:assessment year|ay)\s*(?::|is)?\s*(\d{4}-\d{2,4})'
        ay_matches = re.findall(ay_pattern, text.lower())
        
        if ay_matches:
            details['assessment_year'] = ay_matches[0]
        
        # Extract PAN (for cross-verification)
        pan_matches = re.findall(PATTERNS['pan'], text)
        if pan_matches:
            details['pan_number'] = pan_matches[0]
        
        return details
    
    def process_document(self, file_path):
        """
        Process a single document to extract and validate information
        
        Args:
            file_path (str): Path to the document file
            
        Returns:
            dict: Document analysis results
        """
        # Extract text from document
        text = self.extract_text(file_path)
        if not text:
            return {'is_valid': False, 'error': 'Failed to extract text from document'}
        
        # Determine document type
        doc_type = self.get_document_type(text)
        
        # Process document based on type
        if doc_type == 'aadhaar':
            return self.extract_aadhaar_details(text)
        elif doc_type == 'pan':
            return self.extract_pan_details(text)
        elif doc_type == 'bank_statement':
            return self.analyze_bank_statement(text)
        elif doc_type == 'itr':
            return self.analyze_itr(text)
        else:
            return {'document_type': 'unknown', 'is_valid': False, 'extracted_text': text[:500]}
    
    def fetch_and_process_documents(self, application_id, document_ids):
        """
        Fetch and process multiple documents
        
        Args:
            application_id (str): Loan application ID
            document_ids (list): List of Cloudinary document IDs
            
        Returns:
            dict: Combined analysis results
        """
        results = {
            'application_id': application_id,
            'processing_date': datetime.now().isoformat(),
            'documents': {},
            'extracted_info': {},
            'validation_summary': {
                'valid_documents': 0,
                'invalid_documents': 0,
                'missing_documents': []
            }
        }
        
        required_docs = ['aadhaar', 'pan', 'bank_statement', 'itr']
        processed_doc_types = set()
        
        # Create temporary directory for downloads
        with tempfile.TemporaryDirectory() as temp_dir:
            for doc_id in document_ids:
                # Generate a filename for the document
                file_path = os.path.join(temp_dir, f"{doc_id.split('/')[-1]}")
                
                # Download document from Cloudinary
                downloaded_path = self.download_document(doc_id, file_path)
                if not downloaded_path:
                    results['documents'][doc_id] = {
                        'status': 'download_failed',
                        'error': 'Failed to download document'
                    }
                    continue
                
                # Process the document
                doc_results = self.process_document(downloaded_path)
                doc_type = doc_results.get('document_type')
                
                # Add to processed documents
                if doc_type and doc_type != 'unknown':
                    processed_doc_types.add(doc_type)
                
                # Store document results
                results['documents'][doc_id] = {
                    'status': 'processed',
                    'document_type': doc_type,
                    'is_valid': doc_results.get('is_valid', False),
                    'details': doc_results
                }
                
                # Update validation summary
                if doc_results.get('is_valid', False):
                    results['validation_summary']['valid_documents'] += 1
                else:
                    results['validation_summary']['invalid_documents'] += 1
                
                # Extract key information
                if doc_type == 'aadhaar':
                    if 'aadhaar_number' in doc_results:
                        results['extracted_info']['aadhaar_number'] = doc_results['aadhaar_number']
                    if 'age' in doc_results:
                        results['extracted_info']['age'] = doc_results['age']
                    if 'name' in doc_results:
                        results['extracted_info']['name'] = doc_results['name']
                
                elif doc_type == 'pan':
                    if 'pan_number' in doc_results:
                        results['extracted_info']['pan_number'] = doc_results['pan_number']
                
                elif doc_type == 'itr':
                    if 'annual_income' in doc_results:
                        results['extracted_info']['annual_income'] = doc_results['annual_income']
                
                elif doc_type == 'bank_statement':
                    if 'potential_loan_emis' in doc_results:
                        results['extracted_info']['existing_loans'] = doc_results['potential_loan_emis']
                    if 'closing_balance' in doc_results:
                        results['extracted_info']['bank_balance'] = doc_results['closing_balance']
        
        # Check for missing documents
        for required_doc in required_docs:
            if required_doc not in processed_doc_types:
                results['validation_summary']['missing_documents'].append(required_doc)
        
        # Overall validation status
        if results['validation_summary']['invalid_documents'] == 0 and not results['validation_summary']['missing_documents']:
            results['validation_status'] = 'VALID'
        else:
            results['validation_status'] = 'INVALID'
        
        return results
    
    def save_results(self, application_id, results):
        """
        Save processing results to database
        
        Args:
            application_id (str): Loan application ID
            results (dict): Document processing results
            
        Returns:
            bool: True if successful, False otherwise
        """
        if not self.db:
            logger.warning("Database not configured, skipping save")
            return False
        
        try:
            # Save to MongoDB collection
            collection = self.db.document_validations
            
            # Update if exists, insert if not
            update_result = collection.update_one(
                {'application_id': application_id},
                {'$set': results},
                upsert=True
            )
            
            logger.info(f"Results saved for application {application_id}")
            return True
        except Exception as e:
            logger.error(f"Error saving results: {e}")
            return False

def main():
    """
    Main function for the document processor script
    """
    parser = argparse.ArgumentParser(description='Process loan application documents')
    parser.add_argument('--application_id', required=True, help='Loan application ID')
    parser.add_argument('--document_ids', required=True, help='Comma-separated Cloudinary document IDs')
    parser.add_argument('--output', default='json', choices=['json', 'db'], help='Output format')
    parser.add_argument('--config', default='config.json', help='Path to configuration file')
    
    args = parser.parse_args()
    
    # Load configuration
    try:
        with open(args.config, 'r') as f:
            config = json.load(f)
    except Exception as e:
        logger.error(f"Error loading configuration: {e}")
        sys.exit(1)
    
    # Initialize document processor
    cloudinary_config = config.get('cloudinary', {})
    mongodb_config = config.get('mongodb', {}) if args.output == 'db' else None
    
    processor = DocumentProcessor(cloudinary_config, mongodb_config)
    
    # Process documents
    document_ids = args.document_ids.split(',')
    results = processor.fetch_and_process_documents(args.application_id, document_ids)
    
    # Output results
    if args.output == 'json':
        print(json.dumps(results, indent=2))
    elif args.output == 'db':
        processor.save_results(args.application_id, results)
    
    # Exit with status code based on validation
    if results['validation_status'] == 'VALID':
        sys.exit(0)
    else:
        sys.exit(1)

if __name__ == "__main__":
    main()