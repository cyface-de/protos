# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

The README is the main documentation. It describes the message format, the design decisions, the development commands, the workflows and the release:

@README.adoc

## Rules for Changes

- Keep the README current. When you change the `.proto` file, a command or a workflow, update the README in the same commit.
- Field numbers 1-15 of `Measurement` are reserved for future high-frequency fields. Do not use them for other fields.
- Keep the offset/diff encoding and the units of existing fields. Other repositories (Android and iOS SDK, backend, data collector) decode the data.
- Follow the rules of the workflows in the README section "GitHub Workflows" for each change of `.github/workflows/`.

## Commit Style

`[TICKET-ID] Short imperative summary`.
