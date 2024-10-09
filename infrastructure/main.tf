terraform {
  backend "azurerm" {}

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 4.4.0"
    }
  }
}

provider "azurerm" {
  features {}
}

provider "azurerm" {
  subscription_id            = local.cft_vnet[local.env].subscription
  skip_provider_registration = "true"
  features {}
  alias = "cft_vnet"
}

