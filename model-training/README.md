# Model Training

This folder contains the research and training artifacts used for the Fashnix AI clothing classifier.

## Contents

- `notebooks/fashnix-ai-fashion-app.ipynb` - training notebook with model development workflow.
- `outputs/fashnix_notebook_code_outputs.zip` - exported notebook/code outputs.
- Android runtime assets are included in `app/src/main/assets/`, including TensorFlow Lite models, Keras checkpoints, labels, training curves, sample grids, class distributions, and confusion matrices.

## Runtime Integration

The Android app loads local TensorFlow Lite assets from `app/src/main/assets/` for on-device fashion item classification. Cloud AI styling support is handled separately through Firebase Cloud Functions.

## Reproducibility Notes

Large datasets are not committed in this repository. Keep raw datasets in external storage and document their source, license, and preprocessing steps before sharing publicly.
